package examples.asyncvssync

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import utils.PerformanceTester
import scala.concurrent.duration.Duration

//#content
object AsyncVsSync {

  def syncSum(numbers: Seq[Int]): Int = {
    numbers.reduce(_ + _)
  }

  def asyncSum(numbers: Seq[Future[Int]]): Future[Int] = {
    Future.reduce(numbers)(_ + _)
  }

  def performanceTest = PerformanceTester.compare(3000, 1 to 10000)(
      "Sync" -> (numbers => syncSum(numbers)),
      "Async" -> (numbers => Await.result(
        asyncSum(numbers.map(Future.successful)),
        Duration.Inf
      )
    )
  )
}
//#content

