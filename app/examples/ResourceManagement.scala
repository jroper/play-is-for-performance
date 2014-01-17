package examples

import scala.concurrent.{Await, Future}
import play.api.mvc._
import utils.Benchmark
import play.api.test._
import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Akka
import play.api.Play.current

object ResourceManagement {

  object DumbController extends Controller {

    //#wrap-future
    import play.api.libs.concurrent.Execution.Implicits._

    def myBlockingAction = Action.async {
      Future {
        Thread.sleep(200)
        Ok
      }
    }

    def myNonBlockingAction = Action(Ok)

    def performanceTest = Benchmark.run(5) {
      for (i <- 1 to 20) invoke(myBlockingAction)
      Await.result(invoke(myNonBlockingAction), Duration.Inf)
    }
    //#wrap-future
  }

  object SmartController extends Controller {

    //#custom-executor
    def blockingEc = Akka.system.dispatchers.lookup("blocking")

    def myBlockingAction = Action.async {
      Future {
        Thread.sleep(200)
        Ok
      }(blockingEc)
    }
    //#custom-executor

    def myNonBlockingAction = Action {
      Ok
    }

    def performanceTest = Benchmark.run(5) {
      for (i <- 1 to 20) {
        invoke(myBlockingAction)
      }
      Await.result(invoke(myNonBlockingAction), Duration.Inf)
    }
  }



  def invoke(action: EssentialAction) = action(FakeRequest()).run

}
