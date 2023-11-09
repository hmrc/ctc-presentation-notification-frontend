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
import base.TestMessageData.{consignment, messageData, transitOperation}
import generators.Generators
import models._
import models.messages.Authorisation
import models.messages.AuthorisationType.C521
import navigation.LoadingNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading
import pages.loading._
import pages.transport.{ContainerIndicatorPage, LimitDatePage}

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

      "must go from UnLocodePage to AddExtraInformationYesNoPage" - {
        val userAnswers = emptyUserAnswers.setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
        navigator
          .nextPage(UnLocodePage, userAnswers, departureId, mode)
          .mustBe(AddExtraInformationYesNoPage.route(userAnswers, departureId, mode).value)
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

        "must go from CountryPage to LocationPage when limitDate exists" - {
          val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)

          val userAnswersWithLimitDate = userAnswers.copy(
            departureData =
              messageData.copy(TransitOperation = transitOperation.copy(limitDate = Some("limitDate")), Authorisation = Some(Seq(Authorisation(C521, "1234"))))
          )

          navigator
            .nextPage(CountryPage, userAnswersWithLimitDate, departureId, mode)
            .mustBe(LocationPage.route(userAnswersWithLimitDate, departureId, mode).value)
        }

      }

      "must go from LocationPage to LimitDatePage when limit date does not exist and is simplified" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersNoLimitDate = userAnswers.copy(
          departureData = messageData.copy(TransitOperation = transitOperation.copy(limitDate = None), Authorisation = Some(Seq(Authorisation(C521, "1234"))))
        )

        navigator
          .nextPage(LocationPage, userAnswersNoLimitDate, departureId, mode)
          .mustBe(LimitDatePage.route(userAnswersNoLimitDate, departureId, mode).value)

      }

      "must go from LocationPage to ContainerIndicatorPage when is NOT simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(Consignment = consignment.copy(containerIndicator = None))
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to ContainerIndicatorPage when limit date exists, is simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(Consignment = consignment.copy(containerIndicator = None), Authorisation = Some(Seq(Authorisation(C521, "1234"))))
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }
    }
    // todo update when CYA page built
    "in Check mode" - {
      val mode = CheckMode
      "must go from AddUnLocodeYesNoPage" - {

        "to Unlocode page when answer is Yes and there is no existing UnLocode" in {

          val userAnswers = emptyUserAnswers.setValue(AddUnLocodeYesNoPage, true)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing UnLocode" ignore {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, true)
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
        }

        "to CYA page when answer is NO" ignore {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, false)
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
        }
      }

      "must go from AddExtraInformationYesNoPage " - {

        "to Country page when answer is Yes and there is no existing Country" in {

          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(loading.CountryPage.route(userAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing Country" ignore {

          val userAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, true)
            .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
        }
        "to CYA page when answer is NO" in {

          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(CountryPage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
        }

      }

      "from Country page to CYA page" ignore {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        navigator
          .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
          .mustBe(CountryPage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
      }

      "from UnLocode page to CYA page" ignore {
        val userAnswers = emptyUserAnswers.setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
        navigator
          .nextPage(UnLocodePage, userAnswers, departureId, mode)
          .mustBe(CountryPage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
      }

      "from Location page to CYA page" ignore {
        val userAnswers = emptyUserAnswers.setValue(LocationPage, arbitraryUnLocode.arbitrary.sample.value)
        navigator
          .nextPage(LocationPage, userAnswers, departureId, mode)
          .mustBe(CountryPage.route(userAnswers, departureId, mode).value) //todo update when cya page is built
      }

    }

  }
}
