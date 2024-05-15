/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package navigator

import base.SpecBase
import generators.Generators
import models._
import navigation.Navigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private case object TestBadPage extends Page

  val nonExhaustiveNavigator: Navigator = new Navigator {

    override protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
      case page: QuestionPage[_] => _ => None
    }

    override protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
      case page: QuestionPage[_] => _ => None
    }
  }

  val errorControllerNavigator: Navigator = new Navigator {

    override protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
      case TestBadPage => _ => None
    }

    override protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
      case TestBadPage => _ => None
    }
  }

  "Navigator" - {

    "in Normal mode" - {
      val mode = NormalMode
      "must go to index page with bad navigation" in {
        nonExhaustiveNavigator
          .nextPage(TestBadPage, emptyUserAnswers, departureId, mode)
          .mustBe(controllers.routes.IndexController.redirect(departureId))
      }
      "must go to the error controller when the route returns a None for the page" in {
        errorControllerNavigator
          .nextPage(TestBadPage, emptyUserAnswers, departureId, mode)
          .mustBe(controllers.routes.ErrorController.technicalDifficulties())
      }

    }

    "in Check mode" - {
      val mode = CheckMode
      "must go to index page with bad navigation" in {
        nonExhaustiveNavigator
          .nextPage(TestBadPage, emptyUserAnswers, departureId, mode)
          .mustBe(controllers.routes.IndexController.redirect(departureId))
      }
      "must go to the error controller when the route returns a None for the page" in {
        errorControllerNavigator
          .nextPage(TestBadPage, emptyUserAnswers, departureId, mode)
          .mustBe(controllers.routes.ErrorController.technicalDifficulties())
      }
    }
  }
}
