import java.util.concurrent.Executors
import play.api.{Application, GlobalSettings}
import scala.concurrent.ExecutionContext
import utils.Benchmark

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    Benchmark.executionContext = Some(ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor()))
  }

  override def onStop(app: Application) = {
    Benchmark.executionContext.foreach(_.shutdownNow())
  }
}
