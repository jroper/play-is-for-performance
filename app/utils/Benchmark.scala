package utils

import scala.concurrent._
import scala.Predef._
import java.util.concurrent.atomic.AtomicBoolean

object Benchmark {
  @volatile var executionContext: Option[ExecutionContextExecutorService] = None

  def compare[I](times: Int, input: => I)(tests: (String, I => Unit)*) =
    new Benchmark(times, input, tests)

  def compare(times: Int)(tests: (String, Unit => Unit)*) =
    new Benchmark(times, (), tests)

  def run(times: Int)(test: => Unit) = {
    new Benchmark(times, (), Seq("Test" -> ((u: Unit) => test)))
  }
}

class Benchmark[I](times: Int, input: => I, tests: Seq[(String, I => Unit)]) {

  def start(): PerformanceTestProgressAccessor = {

    val running = new AtomicBoolean(true)

    val testsWithProgress = tests.map {
      case (testName, test) => (testName, test, new Progress)
    }

    implicit val ec = Benchmark.executionContext.getOrElse(ExecutionContext.global)

    val future = Future {
      val results = testsWithProgress.map {
        case (testName, test, progress) =>
          val start = System.currentTimeMillis()
          val theInput = input
          while (progress.times < times && running.get()) {
            test(theInput)
            progress.times += 1
          }
          val time = System.currentTimeMillis() - start
          (testName, time)
      }
      PerformanceTestResults(results)
    }

    new PerformanceTestProgressAccessor {
      def results = future
      def currentProgress = testsWithProgress.map {
        case (testName, test, progress) => (testName, progress.times * 100 / times)
      }
      def stop() = running.set(false)
    }
  }

  class Progress {
    @volatile var times = 0
  }
  
}

trait PerformanceTestProgressAccessor {
  def currentProgress: Seq[(String, Int)]
  def results: Future[PerformanceTestResults]
  def stop(): Unit
}

case class PerformanceTestResults(results: Seq[(String, Long)])
