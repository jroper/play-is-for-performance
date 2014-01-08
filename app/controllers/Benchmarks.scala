package controllers

import play.api.mvc._
import utils.PerformanceTester
import play.api.libs.iteratee.{Enumeratee, Concurrent}
import play.api.libs.json._
import play.api.libs.EventSource
import play.api.libs.EventSource._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.actor.Cancellable
import scala.util.{Failure, Success}
import play.api.Logger
import java.lang.management.ManagementFactory
import play.api.libs.Comet.CometMessage
import examples._
import scala.util.Failure
import scala.Some
import scala.util.Success

object Benchmarks extends Controller {

  def asyncVsSync = benchmark(AsyncVsSync.performanceTest)

  def executionContexts = benchmark(ExecutionContexts.performanceTest)

  def customContexts1 = benchmark(CustomContexts.immediatePerformanceTest)
  def customContexts2 = benchmark(CustomContexts.trampolineTest)

  def resourceManagement1 = benchmark(ResourceManagement.DumbController.performanceTest)
  def resourceManagement2 = benchmark(ResourceManagement.SmartController.performanceTest)

  def prerender1 = benchmark(Prerender.Static.performanceTest)
  def prerender2 = benchmark(Prerender.AlmostStatic.performanceTest)

  def routing = benchmark(Routing.performanceTest)

  case class Event(name: String, data: JsValue)
  object Event {
    def apply[D : Writes](name: String, data: D) = new Event(name, Json.toJson(data))
  }
  case class TestUpdate(tests: Seq[Progress], stats: SystemStats)
  case class Progress(name: String, progress: Int)
  case class SystemStats(cpuUsage: Long, memoryUsage: Long, loadAverage: Double)
  case class TestResults(results: Seq[TestResult])
  case class TestResult(name: String, time: Long)
  case class TestError(className: String, message: Option[String])

  implicit def dataExtractor: CometMessage[Event] = CometMessage(e => Json.stringify(e.data))
  implicit def nameExtractor: EventNameExtractor[Event] = EventNameExtractor(e => Some(e.name))
  implicit def progressWrites = Json.writes[Progress]
  implicit def systemStatsWrites = Json.writes[SystemStats]
  implicit def testUpdate = Json.writes[TestUpdate]
  implicit def testResultWrites = Json.writes[TestResult]
  implicit def testResultsWrites = Json.writes[TestResults]
  implicit def testErrorWrites = Json.writes[TestError]

  def benchmark(tester: PerformanceTester[_]) = Action {

    Logger.info("Starting benchmark")

    val accessor = tester.start()
    
    val poller = Promise[Cancellable]()
    def cancelPoller() = poller.future.foreach(_.cancel())
    
    val enumerator = Concurrent.unicast[Event](onStart = { channel =>

      // Every 200 milliseconds, send a progress update
      poller.success(Akka.system.scheduler.schedule(100.millis, 300.millis) {
        channel.push(Event("progress", TestUpdate(accessor.currentProgress.map(Progress.tupled), getSystemStats)))
      })

      // When the performance test is finished
      accessor.results.onComplete {
        case Success(results) =>
          Logger.info("Benchmark done")
          cancelPoller()
          channel.push(Event("results", TestResults(results.results.map(TestResult.tupled))))
        case Failure(e) =>
          Logger.info("Benchmark error")
          cancelPoller()
          channel.push(Event("error", TestError(e.getClass.getName, Option(e.getMessage))))
      }

    }, onComplete = {
      Logger.info("Client disconnect")
      cancelPoller()
    }, onError = {
      case e => cancelPoller()
    })

    Ok.feed(enumerator &> EventSource[Event]).as("text/event-stream")
  }

  @volatile var lastCpuTime: Long = 0
  @volatile var lastMeasurementTime: Long = 0
  val threads = ManagementFactory.getThreadMXBean
  threads.setThreadCpuTimeEnabled(true)
  val os = ManagementFactory.getOperatingSystemMXBean
  val noCpus = os.getAvailableProcessors
  val memory = ManagementFactory.getMemoryMXBean

  def getSystemStats = {

    val cpuTime = threads.getAllThreadIds.foldLeft(0l) { (total, id) =>
      total + threads.getThreadCpuTime(id)
    }
    val sinceLast = cpuTime - lastCpuTime
    lastCpuTime = cpuTime
    val currentTime = System.currentTimeMillis()
    val time = currentTime - lastMeasurementTime
    lastMeasurementTime = currentTime
    // time is in ms, sinceLast in ns, we want percent, so we multiply by 100 and divide by 1000000, total means divide by 10
    val cpuUsage = Math.max((sinceLast / (time * 10000)) / noCpus, 0)
    val loadAverage = os.getSystemLoadAverage
    val memoryUsage = memory.getHeapMemoryUsage
    val memoryPercent = (memoryUsage.getUsed * 100) / memoryUsage.getMax
    SystemStats(cpuUsage, memoryPercent, loadAverage)

  }
}
