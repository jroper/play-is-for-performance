import java.util.concurrent.Executors
import play.api.{Application, GlobalSettings}
import scala.concurrent.ExecutionContext
import utils.PerformanceTester

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    PerformanceTester.executionContext = Some(ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor()))
  }

  override def onStop(app: Application) = {
    PerformanceTester.executionContext.foreach(_.shutdownNow())
  }
}
