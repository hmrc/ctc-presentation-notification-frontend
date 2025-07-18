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
import config.Constants.AuthorisationTypeDeparture.ACR
import config.Constants.DeclarationTypeSecurity._
import generated._
import generators.Generators
import models._
import models.reference.Country
import navigation.LoadingNavigator
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading
import pages.loading._
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import scalaxb.XMLCalendar

import java.time.LocalDate

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
            .mustEqual(UnLocodePage.route(userAnswers, departureId, mode).value)
        }

        "to Country page when answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(AddUnLocodeYesNoPage, false)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustEqual(CountryPage.route(userAnswers, departureId, mode).value)
        }

      }

      "must go from UnLocodePage to AddExtraInformationYesNoPage" - {
        val userAnswers = emptyUserAnswers.setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value.unLocodeExtendedCode)
        navigator
          .nextPage(UnLocodePage, userAnswers, departureId, mode)
          .mustEqual(AddExtraInformationYesNoPage.route(userAnswers, departureId, mode).value)
      }

      "must go from AddExtraInformationYesNoPage" - {
        "to tech difficulties when AddExtraInformationYesNoPage does not exist" in {
          navigator
            .nextPage(AddExtraInformationYesNoPage, emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }

        "to Country page when answer is Yes" in {
          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustEqual(CountryPage.route(userAnswers, departureId, mode).value)
        }

        "to BorderMode of transport page when answer is No" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddExtraInformationYesNoPage, false)
                .setValue(LimitDatePage, LocalDate.now())
                .copy(departureData =
                  answers.departureData.copy(
                    TransitOperation = answers.departureData.TransitOperation.copy(security = "1"),
                    Consignment = answers.departureData.Consignment.copy(containerIndicator = Some(Number1))
                  )
                )

              navigator
                .nextPage(AddExtraInformationYesNoPage, updatedAnswers, departureId, mode)
                .mustEqual(BorderModeOfTransportPage.route(updatedAnswers, departureId, mode).value)
          }
        }

        "must go from CountryPage to LocationPage when limitDate exists" - {
          val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)

          val userAnswersWithLimitDate = userAnswers.copy(
            departureData = basicIe015.copy(
              TransitOperation = basicIe015.TransitOperation.copy(limitDate = Some(XMLCalendar("2020-01-01T09:30:00")))
            )
          )

          navigator
            .nextPage(CountryPage, userAnswersWithLimitDate, departureId, mode)
            .mustEqual(LocationPage.route(userAnswersWithLimitDate, departureId, mode).value)
        }

      }

      "must go from LocationPage to LimitDatePage when limit date does not exist and is simplified" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersNoLimitDate = userAnswers.copy(
          departureData = basicIe015.copy(
            TransitOperation = basicIe015.TransitOperation.copy(limitDate = None),
            Authorisation = Seq(AuthorisationType02(1, ACR, "1234"))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersNoLimitDate, departureId, mode)
          .mustEqual(LimitDatePage.route(userAnswersNoLimitDate, departureId, mode).value)
      }

      "must go from LocationPage to BorderModePage when is simplified and limit date exists and container indicator exists" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          .setValue(LimitDatePage, LocalDate.now())
        val userAnswersWithLimitDate = userAnswers.copy(
          departureData = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(containerIndicator = Some(Number1)),
            TransitOperation = basicIe015.TransitOperation.copy(limitDate = Some(XMLCalendar("2020-01-01T09:30:00")))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersWithLimitDate, departureId, mode)
          .mustEqual(BorderModeOfTransportPage.route(userAnswersWithLimitDate, departureId, mode).value)

      }

      "must go from LocationPage to ContainerIndicatorPage when is NOT simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(containerIndicator = None)
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustEqual(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to BorderModePage when is NOT simplified and container indicator is present" in {

        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(containerIndicator = Some(Number1))
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustEqual(BorderModeOfTransportPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to ContainerIndicatorPage when limit date exists, is simplified and container indicator is empty" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          .setValue(LimitDatePage, LocalDate.now())
        val userAnswersUpdated = userAnswers.copy(
          departureData = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(containerIndicator = None)
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustEqual(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to BorderModePage when is NOT simplified and container indicator is present and limit date is not present" in {

        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(containerIndicator = Some(Number1)),
            TransitOperation = basicIe015.TransitOperation.copy(limitDate = None)
          )
        )

        navigator
          .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
          .mustEqual(BorderModeOfTransportPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LocationPage to CheckYourAnswers page when " +
        "it is NOT simplified and " +
        "container indicator is present in Departure Data and " +
        "limit date is not present" +
        "and security is 0" in {
          val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          val userAnswersUpdated = userAnswers.copy(
            departureData = basicIe015.copy(
              Consignment = basicIe015.Consignment.copy(containerIndicator = Some(Number1)),
              TransitOperation = basicIe015.TransitOperation.copy(limitDate = None, security = NoSecurityDetails)
            )
          )

          navigator
            .nextPage(LocationPage, userAnswersUpdated, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
    }

    "in Check mode" - {
      val mode = CheckMode
      "must go from AddUnLocodeYesNoPage" - {

        "to Unlocode page when answer is Yes and there is no existing UnLocode in either the 15/13/170" in {

          val ie015WithNoUnLocodeUserAnswers = emptyUserAnswers
            .copy(departureData = basicIe015.copy(Consignment = basicIe015.Consignment.copy(PlaceOfLoading = None)))
            .setValue(AddUnLocodeYesNoPage, true)
          navigator
            .nextPage(AddUnLocodeYesNoPage, ie015WithNoUnLocodeUserAnswers, departureId, mode)
            .mustEqual(UnLocodePage.route(ie015WithNoUnLocodeUserAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing UnLocode" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, true)
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value.unLocodeExtendedCode)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to Country page when answer is No" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, false)
          navigator
            .nextPage(AddUnLocodeYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.loading.routes.CountryController.onPageLoad(departureId, mode))
        }
      }

      "must go from AddExtraInformationYesNoPage " - {

        "to Country page when answer is Yes and there is no existing Country in 15/13/170" in {

          val ie015WithNoCountryUserAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(AddExtraInformationYesNoPage, ie015WithNoCountryUserAnswers, departureId, mode)
            .mustEqual(loading.CountryPage.route(ie015WithNoCountryUserAnswers, departureId, mode).value)
        }

        "to CYA page when answer is Yes and there is an existing Country" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, true)
            .setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
        "to CYA page when answer is NO" in {

          val userAnswers = emptyUserAnswers.setValue(AddExtraInformationYesNoPage, false)
          navigator
            .nextPage(AddExtraInformationYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from Country page" - {
        "to Location Page" in {
          forAll(Arbitrary.arbitrary[Country].sample.value) {
            country =>
              val userAnswers = emptyUserAnswers.setValue(CountryPage, country)
              navigator
                .nextPage(CountryPage, userAnswers, departureId, mode)
                .mustEqual(LocationPage.route(userAnswers, departureId, mode).value)
          }
        }
      }

      "must go from Unlocode page" - {
        "to AddExtraInformationPage when the AddExtraInformationPage does not exist in either the 13/15/170" in {
          val ie015WithNoExtraInformationUserAnswers = emptyUserAnswers
            .setValue(AddUnLocodeYesNoPage, true)
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value.unLocodeExtendedCode)
          navigator
            .nextPage(UnLocodePage, ie015WithNoExtraInformationUserAnswers, departureId, mode)
            .mustEqual(AddExtraInformationYesNoPage.route(ie015WithNoExtraInformationUserAnswers, departureId, mode).value)
        }

        "to CYAPage when the addExtraInformationPage does exist in either the 13/15/170" in {
          val withAddExtraInformationUserAnswers = emptyUserAnswers
            .setValue(UnLocodePage, arbitraryUnLocode.arbitrary.sample.value.unLocodeExtendedCode)
          navigator
            .nextPage(UnLocodePage, withAddExtraInformationUserAnswers, departureId, mode)
            .mustEqual(AddExtraInformationYesNoPage.route(withAddExtraInformationUserAnswers, departureId, CheckMode).value)
        }
        "to CYAPage when AddExtraInformationYesNoPage exists" in {
          val withAddExtraInformationUserAnswers = emptyUserAnswers
            .setValue(AddExtraInformationYesNoPage, true)
          navigator
            .nextPage(UnLocodePage, withAddExtraInformationUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "from Location page to CYA page" in {
        val userAnswers = emptyUserAnswers.setValue(LocationPage, nonEmptyString.sample.value)
        navigator
          .nextPage(LocationPage, userAnswers, departureId, mode)
          .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }

    }

  }
}
