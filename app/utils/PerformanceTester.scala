package utils

import scala.concurrent._
import scala.Predef._

object PerformanceTester {
  @volatile var executionContext: Option[ExecutionContextExecutorService] = None

  def compare[I](times: Int, input: I)(tests: (String, I => Unit)*) =
    new PerformanceTester(times, input, tests)
}

class PerformanceTester[I](times: Int, input: I, tests: Seq[(String, I => Unit)]) {
  
  def start(): PerformanceTestProgressAccessor = {
    
    val testsWithProgress = tests.map {
      case (testName, test) => (testName, test, new Progress)
    }

    implicit val ec = PerformanceTester.executionContext.getOrElse(ExecutionContext.global)

    val future = Future {
      val results = testsWithProgress.map {
        case (testName, test, progress) =>
          val start = System.currentTimeMillis()
          for (i <- 1 to times) {
            test(input)
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
    }
  }

  class Progress {
    @volatile var times = 0
  }
  
}

trait PerformanceTestProgressAccessor {
  def currentProgress: Seq[(String, Int)]
  def results: Future[PerformanceTestResults]
}

case class PerformanceTestResults(results: Seq[(String, Long)])