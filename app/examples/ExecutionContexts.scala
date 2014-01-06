package examples

import scala.concurrent.ExecutionContext
import java.util.concurrent.{Executors, CountDownLatch}
import utils.PerformanceTester

object ExecutionContexts {

  type B = String
  type Foo[_] = List[_]

  //#map-ec
  def map[A](f: B => A)
            (implicit ctx: ExecutionContext): Foo[A] = {
    //#map-ec
    Nil
  }

  //#ec-methods
  def immediate(latch: CountDownLatch, nextAction: () => Unit) = {
    latch.countDown()
    nextAction()
  }

  def asynchronous(latch: CountDownLatch, nextAction: () => Unit)
                   (implicit ctx: ExecutionContext) = {
    latch.countDown()
    ctx.execute(new Runnable() {
      def run() = nextAction()
    })
  }

  def notSoImportantOp() = Thread.sleep(1)
  //#ec-methods

  //#ec-test
  def performanceTest = PerformanceTester.compare(100, 100)(
    "No-ExecutionContext" -> { times =>
      val latch = new CountDownLatch(times)
      asynchronously {
        for (i <- 1 to times) {
          immediate(latch, notSoImportantOp)
        }
      }
      latch.await()
    },
    "ExecutionContext" -> { times =>
      withNewEc { implicit ctx =>
        val latch = new CountDownLatch(times)
        asynchronously {
          for (i <- 1 to times) {
            asynchronous(latch, notSoImportantOp)
          }
        }
        latch.await()
      }
    }
  )
  //#ec-test

  def asynchronously(block: => Unit) = {
    new Thread(new Runnable() {
      def run() = block
    }).start()
  }

  def withNewEc(block: ExecutionContext => Unit) = {
    val ec = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())
    try {
      block(ec)
    } finally {
      ec.shutdownNow()
    }
  }

}
