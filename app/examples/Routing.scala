package examples

import play.core.Router
import utils.PerformanceTester
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.mvc.EssentialAction

object Routing {

  //#router
  object Custom extends Router.Routes {
    def routes = Function.unlift { req =>
      req.path match {
        case fooPath if fooPath.startsWith(prefix + "/foo") =>
          foo.Routes.routes.lift(req)
        case barPath if barPath.startsWith(prefix + "/bar") =>
          bar.Routes.routes.lift(req)
        case otherPath if otherPath.startsWith(prefix + "/other") =>
          other.Routes.routes.lift(req)
        case _ => None
      }
    }

    var _prefix = ""
    def setPrefix(p: String) = {
      _prefix = if (p == "/") "" else p
      foo.Routes.setPrefix(prefix + "/foo")
      bar.Routes.setPrefix(prefix + "/bar")
      other.Routes.setPrefix(prefix + "/other")
    }
    def prefix = _prefix
    def documentation = Nil
  }
  //#router

  //#test
  def runTest(router: Router.Routes) = {
    invoke(router, "/foo/a")
    invoke(router, "/bar/a")
    invoke(router, "/other/a")
  }

  def performanceTest = PerformanceTester.compare(10000, ())(
    "Custom" -> { _ =>
      Custom.setPrefix("/")
      runTest(Custom)
    },
    "Built-In" -> { _ =>
      combined.Routes.setPrefix("/")
      runTest(combined.Routes)
    }
  )
  //#test

  def invoke(router: Router.Routes, path: String) = {
    val req = FakeRequest("GET", path)
    router.routes(req) match {
      case action: EssentialAction => status(action(req).run)
    }
  }
}
