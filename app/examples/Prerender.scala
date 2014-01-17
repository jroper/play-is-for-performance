package examples

import play.api.mvc._
import utils.Benchmark
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.i18n.Lang
import play.api.Play.current

object Prerender {

  object Static extends Controller {

    //#static
    def normal = Action {
      Ok(views.html.someTemplate())
    }

    val prerendered = {
      val result = Ok(views.html.someTemplate())
      Action(result)
    }

    def performanceTest = Benchmark.compare(1000, ())(
      "Pre-rendered" -> { _ =>
        contentAsString(prerendered(FakeRequest()))
      },
      "Normal" -> { _ =>
        contentAsString(normal(FakeRequest()))
      }
    )
    //#static
  }

  object AlmostStatic extends Controller {

    //#almost
    def normal = Action { implicit req =>
      Ok(views.html.langDependentTemplate())
        .withHeaders(CONTENT_LANGUAGE -> lang.code)
    }

    val prerendered = {
      val results = Lang.availables.map { implicit lang =>
        lang -> Ok(views.html.langDependentTemplate())
          .withHeaders(CONTENT_LANGUAGE -> lang.code)
      }.toMap
      Action(req => results(Lang.preferred(req.acceptLanguages)))
    }

    def performanceTest = Benchmark.compare(1000, ())(
      "Pre-rendered" -> { _ =>
        contentAsString(prerendered(FakeRequest()))
      },
      "Normal" -> { _ =>
        contentAsString(normal(FakeRequest()))
      }
    )
    //#almost
  }

}