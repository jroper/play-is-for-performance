package examples

import play.api.libs.iteratee.{Step, Enumerator, Iteratee}
import scala.concurrent.{Await, ExecutionContext}
import utils.PerformanceTester
import java.util.{ArrayDeque, Deque}
import scala.concurrent.duration.Duration

object CustomContexts {

  //#add-iteratee
  def add(elements: Int)
         (implicit ctx: ExecutionContext) = {
    Await.ready(
      Enumerator.enumerate(1 to elements) |>>>

        Iteratee.fold(0) {
          case (total, e) => total + e
        }
    , Duration.Inf)
  }
  //#add-iteratee

  //#immediate-ec
  val immediateExecutionContext = new ExecutionContext {

    def execute(runnable: Runnable) = runnable.run()

    def reportFailure(t: Throwable) = t.printStackTrace()
  }
  //#immediate-ec

  //#immediate-test
  val immediatePerformanceTest = PerformanceTester.compare(2000, 200)(
    "Default" -> { times =>
      import scala.concurrent.ExecutionContext.Implicits.global
      add(times)
    },
    "Immediate" -> { times =>
      implicit def ec = immediateExecutionContext
      add(times)
    }
  )
  //#immediate-test

  //#trampoline-ec
  val trampolineExecutionContext = new ExecutionContext {
    private val local = new ThreadLocal[Deque[Runnable]]

    def execute(runnable: Runnable): Unit = {
      var queue = local.get()
      if (queue == null) {
        try {
          queue = new ArrayDeque(4)
          queue.addLast(runnable)
          local.set(queue)
          while (!queue.isEmpty) {
            val runnable = queue.removeFirst()
            runnable.run()
          }
        } finally {
          local.set(null)
        }
      } else {
        queue.addLast(runnable)
      }
    }

    def reportFailure(t: Throwable) = t.printStackTrace()
  }
  //#trampoline-ec

  //#trampoline-test
  val trampolineTest = PerformanceTester.compare(200, 2000)(
    "Default" -> { times =>
      import scala.concurrent.ExecutionContext.Implicits.global
      add(times)
    },
    "Trampoline" -> { times =>
      implicit def ec = trampolineExecutionContext
      add(times)
    }
  )
  //#trampoline-test


}
