package examples

import utils.PerformanceTester
import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import play.api.test._
import play.api.test.Helpers._
import play.filters.csrf._
import scala.Predef._

object CsrfSolutions {

  implicit val ec = CustomContexts.trampolineExecutionContext

  val defaultBody = (1 to 1000).map { i => "field" + i + "=value" + i }.mkString("&")

  object QueryString extends Controller {

    def testCsrfFilter(request: RequestHeader, body: String) = {
      assert(status(
        Enumerator.enumerate(body.getBytes.grouped(1000)) |>>> CSRFFilter()(Action(_ => Ok))(request)
      ) == 200)
    }

    //#query-string
    def performanceTest = PerformanceTester.compare(1000)(
      "Form-Field" -> { _ =>
        testCsrfFilter(FakeRequest("POST", "/save")
          .withHeaders("Content-Type" -> "application/x-www-form-url-encoded")
          .withSession("csrfToken" -> "foo"),
          defaultBody + "&csrfToken=foo"
        )
      },
      "Query-String" -> { _ =>
        testCsrfFilter(FakeRequest("POST", "/save?csrfToken=foo")
          .withHeaders("Content-Type" -> "application/x-www-form-url-encoded")
          .withSession("csrfToken" -> "foo"),
          defaultBody
        )
      }
    )
    //#query-string

  }

  object CsrfAction extends Controller {

    def testCsrfFilter(action: EssentialAction) = {

      val req = FakeRequest("POST", "/save")
        .withHeaders("Content-Type" -> "application/x-www-form-url-encoded")
        .withSession("csrfToken" -> "foo")

      val body = defaultBody + "&csrfToken=foo"

      assert(status(
        Enumerator.enumerate(body.getBytes.grouped(1000)) |>>> action(req)
      ) == 200)
    }

    //#csrf-check-action
    def viewForm = CSRFAddToken {
      Action(_ => Ok)
    }

    def save = CSRFCheck {
      Action(_ => Ok)
    }
    //#csrf-check-action

    //#csrf-check-test
    def performanceTest = PerformanceTester.compare(1000)(
      "CSRFFilter" -> { _ =>
        testCsrfFilter(CSRFFilter()(Action(_ => Ok)))
      },
      "CSRFCheck-Action" -> { _ =>
        testCsrfFilter(save)
      }
    )
    //#csrf-check-test

  }


}
