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
import navigation.LoadingNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading._

class LoadingNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new LoadingNavigator

  "LoadingNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode
      "must go from AddUnLocodeYesNoPage" - {

        "to Unlocode page when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(AddUnLocodeYesNoPage, true)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value)
        }

        "to Country page when answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(AddUnLocodeYesNoPage, false)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(CountryPage.route(userAnswers, departureId, mode).value)
        }

      }

      "must go from UnLocodePage to CountryPage" - {
        val userAnswers = emptyUserAnswers.setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
        navigator
          .nextPage(UnLocodePage, userAnswers, departureId, mode)
          .mustBe(CountryPage.route(userAnswers, departureId, mode).value)
      }

      "must go from AddExtraInformationYesNoPage" - {

        "to Country page when answer is Yes" in {
          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(CountryPage.route(userAnswers, departureId, mode).value)
        }

        "to Location page when answer is No" in {
          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, false)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(LocationPage.route(userAnswers, departureId, mode).value)
        }

        "must go from CountryPage to LocationPage" - {
          val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          navigator
            .nextPage(CountryPage, userAnswers, departureId, mode)
            .mustBe(LocationPage.route(userAnswers, departureId, mode).value)
        }

      }

    }
// todo update when CYA page built
//    "in Check mode" - {
//      val mode = CheckMode
//      "must go from AddUnLocodeYesNoPage" - {
//
//        "to Unlocode page when answer is Yes and there is no existing UnLocode" in {
//
//          val userAnswers = emptyUserAnswers.setValue(AddUnLocodeYesNoPage, true)
//          navigator
//            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
//            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value)
//        }
//
//        "to Country page when answer is No" in {
//
//          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, false)
//          navigator
//            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
//            .mustBe(CountryPage.route(userAnswers, departureId, mode).value)
//        }
//
//      }
//
//    }

  }
}
