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

import base.TestMessageData.{consignment, messageData, transitOperation}
import base.{SpecBase, TestMessageData}
import config.Constants._
import generators.Generators
import models._
import models.messages.AuthorisationType.C521
import models.messages.{Authorisation, MessageData}
import navigation.LocationOfGoodsNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.Page
import pages.loading.CountryPage
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}

class LocationOfGoodsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new LocationOfGoodsNavigator

  "LocationOfGoodsNavigator" - {

    "in Normal mode" - {

      val mode = NormalMode
      "must go from LocationTypePage to IdentificationPage" - {

        "when value is inferred" in {
          val value = arbitrary[LocationType].sample.value

          val userAnswers = emptyUserAnswers.setValue(InferredLocationTypePage, value)
          navigator
            .nextPage(InferredLocationTypePage, userAnswers, departureId, mode)
            .mustBe(IdentificationPage.route(userAnswers, departureId, mode).value)
        }

        "when value is not inferred" in {
          val value = arbitrary[LocationType].sample.value

          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, value)
          navigator
            .nextPage(LocationTypePage, userAnswers, departureId, mode)
            .mustBe(IdentificationPage.route(userAnswers, departureId, mode).value)
        }
      }

      "must go from IdentificationPage to next page" - {
        Seq[String](
          CustomsOfficeIdentifier,
          EoriNumberIdentifier,
          AuthorisationNumberIdentifier,
          UnlocodeIdentifier,
          CoordinatesIdentifier,
          AddressIdentifier,
          PostalCodeIdentifier
        ) foreach (
          identifier =>
            s"when value is $identifier" in {
              val value: LocationOfGoodsIdentification = LocationOfGoodsIdentification(identifier, "identifier")

              val userAnswers = emptyUserAnswers.setValue(IdentificationPage, value)
              navigator
                .nextPage(IdentificationPage, userAnswers, departureId, mode)
                .mustBe(navigator.routeIdentificationPageNavigation(userAnswers, departureId, mode).value)
            }
        )
      }

      "redirect to LocationTypeController when locationOfGoods is None and not simplified" in {

        val userAnswers                = arbitraryUserData.arbitrary.sample.value
        val consignment                = userAnswers.departureData.Consignment.copy(LocationOfGoods = None)
        val departureData: MessageData = userAnswers.departureData.copy(Authorisation = None, Consignment = consignment)
        val simplifiedUserAnswers      = userAnswers.copy(departureData = departureData)

        val result = navigator.locationOfGoodsNavigation(simplifiedUserAnswers, departureId, mode).get
        result.mustBe(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode))
      }

      "redirect to AuthorisationNumberController when locationOfGoods is None and is simplified" in {

        val userAnswers                = arbitraryUserData.arbitrary.sample.value
        val consignment                = userAnswers.departureData.Consignment.copy(LocationOfGoods = None)
        val departureData: MessageData = userAnswers.departureData.copy(Consignment = consignment)
        val simplifiedUserAnswers      = userAnswers.copy(departureData = departureData)

        val result = navigator.locationOfGoodsNavigation(simplifiedUserAnswers, departureId, mode).get
        result.mustBe(controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode))
      }

      "must go from EORI Page to Add Additional Identifier Yes No page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EoriPage, answers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go from Authorisation Number Page to Add Additional Identifier Yes No page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AuthorisationNumberPage, answers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go from Add AdditionalIdentifierYesNo page to AdditionalIdentifier page when user selects Yes" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddIdentifierYesNoPage, true)
            navigator
              .nextPage(AddIdentifierYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.routes.AdditionalIdentifierController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go from Add AdditionalIdentifierYesNo page to AddContactYesNo page when user selects No" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddIdentifierYesNoPage, false)
            navigator
              .nextPage(AddIdentifierYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.routes.AddContactYesNoController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go to AddContact next page" - {
        Seq[Page](
          AdditionalIdentifierPage,
          CoordinatesPage,
          UnLocodePage,
          AddressPage,
          PostalCodePage
        ) foreach (
          page =>
            s"when page is $page" in {
              forAll(arbitrary[UserAnswers]) {
                answers =>
                  navigator
                    .nextPage(page, answers, departureId, NormalMode)
                    .mustBe(controllers.locationOfGoods.routes.AddContactYesNoController.onPageLoad(departureId, NormalMode))
              }
            }
        )
      }

      "must go from Add AddContactYesNo page to ContactName page when user selects Yes" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, true)
            navigator
              .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.contact.routes.NameController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go from Add AddContactYesNo page to AddUnLocode page when user selects No and POL does not exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, false)
              .copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(PlaceOfLoading = None)))

            navigator
              .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(AddUnLocodePage.route(answers, departureId, mode).value)
        }
      }

      "must go from Add AddContactYesNo page to ContainerIndicatorPage page when user selects No and POL & limit date exists and Container Indicator does not exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, false)
              .copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(containerIndicator = None)))

            navigator
              .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(ContainerIndicatorPage.route(answers, departureId, mode).value)
        }
      }

      "must go from Add AddContactYesNo page to ContainerIdentificationNumberPage page when user selcts no, Security is NoSecurityDetails, Add contcact if false, Container Indicator is true" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, false)
              .setValue(ContainerIndicatorPage, true)
              .copy(departureData =
                TestMessageData.messageData.copy(
                  TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                )
              )

            navigator
              .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
              .mustBe(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))
        }
      }

      "must go from ContactName page to Contact phone number page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(NamePage, answers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.contact.routes.PhoneNumberController.onPageLoad(departureId, NormalMode))
        }
      }

      "must go from CustomsOfficeIdentifierPage to AddUnLocodePage when place of loading is not present" in {

        val userAnswers = emptyUserAnswers.copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(PlaceOfLoading = None)))

        navigator
          .nextPage(CustomsOfficeIdentifierPage, userAnswers, departureId, mode)
          .mustBe(AddUnLocodePage.route(userAnswers, departureId, mode).value)
      }

      "must go from AddContactYesNoPage to AddUnLocodePage when 'addContact' is false and place of loading is not present" in {
        val userAnswers         = emptyUserAnswers.setValue(AddContactYesNoPage, false)
        val userAnswersEmptyPOL = userAnswers.copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(PlaceOfLoading = None)))

        navigator
          .nextPage(AddContactYesNoPage, userAnswersEmptyPOL, departureId, mode)
          .mustBe(AddUnLocodePage.route(userAnswersEmptyPOL, departureId, mode).value)
      }

      "must go from PhoneNumberPage to AddUnLocodePage when 'placeOfLoading' exists and place of loading is not present" in {

        val identifier: LocationOfGoodsIdentification = LocationOfGoodsIdentification(UnlocodeIdentifier, "identifier")
        val userAnswers                               = emptyUserAnswers.setValue(IdentificationPage, identifier)
        val userAnswersEmptyPOL                       = userAnswers.copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(PlaceOfLoading = None)))

        navigator
          .nextPage(PhoneNumberPage, userAnswersEmptyPOL, departureId, mode)
          .mustBe(AddUnLocodePage.route(userAnswersEmptyPOL, departureId, mode).value)
      }

      "must go from Add PhoneNumberPage page to ContainerIndicatorPage page when user selects No and POL & limit date exists and Container Indicator does not exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, false)
              .copy(departureData = TestMessageData.messageData.copy(Consignment = consignment.copy(containerIndicator = None)))

            navigator
              .nextPage(PhoneNumberPage, updatedAnswers, departureId, NormalMode)
              .mustBe(ContainerIndicatorPage.route(answers, departureId, mode).value)
        }
      }

      "must go from CustomsOfficeIdentifierPage to LimitDatePage when place of loading is present and limit date does not exist and is simplified" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .copy(departureData =
                TestMessageData.messageData.copy(TransitOperation = transitOperation.copy(limitDate = None),
                                                 Authorisation = Some(Seq(Authorisation(C521, "1234")))
                )
              )
            navigator
              .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, mode)
              .mustBe(LimitDatePage.route(updatedAnswers, departureId, mode).value)
        }
      }

      "must go from AddContactYesNoPage to LimitDatePage when 'addContact' is false and place of loading is present and limit date does not exist and is simplified" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddContactYesNoPage, false)
              .copy(departureData =
                TestMessageData.messageData.copy(TransitOperation = transitOperation.copy(limitDate = None),
                                                 Authorisation = Some(Seq(Authorisation(C521, "1234")))
                )
              )
            navigator
              .nextPage(AddContactYesNoPage, updatedAnswers, departureId, mode)
              .mustBe(LimitDatePage.route(updatedAnswers, departureId, mode).value)
        }
      }

      "must go from PhoneNumberPage to LimitDatePage when place of loading is present and limit date does not exist and is simplified" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .copy(departureData =
                TestMessageData.messageData.copy(TransitOperation = transitOperation.copy(limitDate = None),
                                                 Authorisation = Some(Seq(Authorisation(C521, "1234")))
                )
              )
            navigator
              .nextPage(PhoneNumberPage, updatedAnswers, departureId, mode)
              .mustBe(LimitDatePage.route(updatedAnswers, departureId, mode).value)
        }
      }

      "must go from LimitDatePage to ContainerIndicatorPage when container indicator is empty" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(Consignment = consignment.copy(containerIndicator = None))
        )

        navigator
          .nextPage(LimitDatePage, userAnswersUpdated, departureId, mode)
          .mustBe(ContainerIndicatorPage.route(userAnswersUpdated, departureId, mode).value)
      }

      "must go from LimitDatePage to BorderModeOfTransportPage when container indicator is present" in {
        val userAnswers = emptyUserAnswers.setValue(CountryPage, arbitraryCountry.arbitrary.sample.value)
        val userAnswersUpdated = userAnswers.copy(
          departureData = messageData.copy(Consignment = consignment.copy(containerIndicator = Some("indicator")))
        )

        navigator
          .nextPage(LimitDatePage, userAnswersUpdated, departureId, mode)
          .mustBe(BorderModeOfTransportPage.route(userAnswersUpdated, departureId, mode).value)
      }
      "must go from Add PhoneNumberPage page to ContainerIdentificationNumberPage page " +
        "when user selects No and POL & limit date exists and Container Indicator exists" +
        "and security is '0'" +
        "and active border means is present" +
        "and container indicator is '1'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddContactYesNoPage, false)
                .setValue(ContainerIndicatorPage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(PhoneNumberPage, updatedAnswers, departureId, NormalMode)
                .mustBe(ContainerIdentificationNumberPage(equipmentIndex).route(answers, departureId, mode).value)
          }
        }
      "must go from Add PhoneNumberPage page to AddTransportEquipmentYesNoPage" +
        "when user selects No" +
        "and POL & limit date exists" +
        "and security is '0'" +
        "and active border means is present" +
        "and container indicator is '0'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddContactYesNoPage, false)
                .setValue(ContainerIndicatorPage, false)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(PhoneNumberPage, updatedAnswers, departureId, NormalMode)
                .mustBe(AddTransportEquipmentYesNoPage.route(answers, departureId, mode).value)
          }
        }
    }
  }
}
