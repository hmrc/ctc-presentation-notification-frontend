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
import base.TestMessageData.{allOptionsNoneJsonValue, consignment, messageData, transitOperation}
import generators.Generators
import models._
import models.messages.AuthorisationType.C521
import models.messages.{Authorisation, AuthorisationType, MessageData}
import models.reference.Country
import navigation.LoadingNavigator
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading
import pages.loading._
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.libs.json.Json

import java.time.{Instant, LocalDate}

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

        "to BorderMode of transport page when answer is No" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, false)
            .setValue(LimitDatePage, LocalDate.now())

          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(BorderModeOfTransportPage.route(userAnswers, departureId, mode).value)
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

      "must go from LocationPage to BorderModePage when is simplified and limit date exists and container indicator exists" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          .setValue(LimitDatePage, LocalDate.now())
        val userAnswersWithLimitDate = userAnswers.copy(
          departureData = messageData.copy(
            Consignment = consignment.copy(containerIndicator = Some("indicator")),
            TransitOperation = transitOperation.copy(limitDate = Some("date")),
            Authorisation = Some(Seq(Authorisation(C521, "1234")))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersWithLimitDate, departureId, mode)
          .mustBe(BorderModeOfTransportPage.route(userAnswersWithLimitDate, departureId, mode).value)

      }

      "must go from LocationPage to ContainerIndicatorPage when is NOT simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(
            Consignment = consignment.copy(containerIndicator = None),
            Authorisation = Some(Seq(Authorisation(AuthorisationType.Other("C999"), "1234")))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to BorderModePage when is NOT simplified and container indicator is present" in {

        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(
            Consignment = consignment.copy(containerIndicator = Some("indicator")),
            Authorisation = Some(Seq(Authorisation(AuthorisationType.Other("C999"), "1234")))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(BorderModeOfTransportPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to ContainerIndicatorPage when limit date exists, is simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          .setValue(LimitDatePage, LocalDate.now())
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(Consignment = consignment.copy(containerIndicator = None), Authorisation = Some(Seq(Authorisation(C521, "1234"))))
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to BorderModePage when is NOT simplified and container indicator is present and limit date is not present" in {

        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(
            Consignment = consignment.copy(containerIndicator = Some("indicator")),
            TransitOperation = transitOperation.copy(limitDate = None),
            Authorisation = Some(Seq(Authorisation(AuthorisationType.Other("C999"), "1234")))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustBe(BorderModeOfTransportPage.route(userAnswersUpdated, departureId, mode).value)
      }

    }
    "in Check mode" - {
      val mode = CheckMode
      "must go from AddUnLocodeYesNoPage" - {

        "to Unlocode page when answer is Yes and there is no existing UnLocode in either the 15/13/170" in {

          val ie015WithNoUnLocodeUserAnswers =
            UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(AddUnLocodeYesNoPage, true)
          navigator
            .nextPage(AddUnLocodeYesNoPage, ie015WithNoUnLocodeUserAnswers, departureId, mode)
            .mustBe(UnLocodePage.route(ie015WithNoUnLocodeUserAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing UnLocode" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, true)
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to Country page when answer is No" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, false)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustBe(controllers.loading.routes.CountryController.onPageLoad(departureId, mode))
        }
      }

      "must go from AddExtraInformationYesNoPage " - {

        "to Country page when answer is Yes and there is no existing Country in 15/13/170" in {

          val ie015WithNoCountryUserAnswers =
            UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, ie015WithNoCountryUserAnswers, departureId, mode)
            .mustBe(loading.CountryPage.route(ie015WithNoCountryUserAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing Country" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, true)
            .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
        "to CYA page when answer is NO" in {

          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, false)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from Country page" - {
        "to Location Page" in {
          forAll(Arbitrary.arbitrary[Country].sample.value) {
            country =>
              val userAnswers = emptyUserAnswers.setValue(CountryPage, country)
              navigator
                .nextPage(CountryPage, userAnswers, departureId, mode)
                .mustBe(LocationPage.route(userAnswers, departureId, mode).value)
          }
        }
      }

      "must go from Unlocode page" - {
        "to AddExtraInformationPage when the AddExtraInformationPage does not exist in either the 13/15/170" in {
          val ie015WithNoExtraInformationUserAnswers =
            UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(AddUnLocodeYesNoPage, true)
              .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
          navigator
            .nextPage(UnLocodePage, ie015WithNoExtraInformationUserAnswers, departureId, mode)
            .mustBe(AddExtraInformationYesNoPage.route(ie015WithNoExtraInformationUserAnswers, departureId, mode).value)
        }

        "to CYAPage when the addExtraInformationPage does exist in either the 13/15/170" in {
          val withAddExtraInformationUserAnswers = emptyUserAnswers
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value)
          navigator
            .nextPage(UnLocodePage, withAddExtraInformationUserAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "from Location page to CYA page" in {
        val userAnswers = emptyUserAnswers.setValue(LocationPage, nonEmptyString.sample.value)
        navigator
          .nextPage(LocationPage, userAnswers, departureId, mode)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }

    }

  }
}
