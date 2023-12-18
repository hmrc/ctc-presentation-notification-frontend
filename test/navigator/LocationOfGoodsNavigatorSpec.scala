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

import base.TestMessageData.{consignment, locationOfGoods, messageData, transitOperation}
import base.{SpecBase, TestMessageData}
import config.Constants._
import generators.Generators
import models._
import models.messages.AuthorisationType.{C521, C523}
import models.messages.{Authorisation, MessageData}
import models.reference.{BorderMode, Country}
import navigation.LocationOfGoodsNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading.CountryPage
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import pages.{AddPlaceOfLoadingYesNoPage, MoreInformationPage, Page}

class LocationOfGoodsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new LocationOfGoodsNavigator

  "LocationOfGoodsNavigator" - {

    "in Normal Mode" - {

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

      "must go from MoreInformationPage to ContainerIdentificationNumberPage " +
        "when user hits 'Save and Continue', " +
        "consignment contains LocationOfGoods, " +
        "Security is NoSecurityDetails, " +
        "Container Indicator is true" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(MoreInformationPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))
          }
        }

      "must go from MoreInformationPage to Check Your Answers when: " +
        "Place of loading is present AND container indicator is NOT captured in IE170 AND " +
        "is NOT C521 AND container indicator is present (in 13/15) AND security is 0 AND Mode of transport is 5" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Mail, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C523, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from MoreInformationPage to Check Your Answers when: " +
        "Place of loading is present AND container indicator is NOT captured in IE170 AND " +
        "is NOT C521 AND container indicator is present (in 13/15) AND security is 0 AND Mode of transport is 4 AND transport means is present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C523, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from MoreInformationPage to Check Your Answers when: " +
        "Place of loading is present AND container indicator is NOT captured in IE170 AND " +
        "is C521 AND limit date exists AND container indicator is present (in 13/15) AND security is 0 AND Mode of transport is 5" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Mail, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C521, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from MoreInformationPage to Check Your Answers when: " +
        "Place of loading is present AND container indicator is NOT captured in IE170 AND " +
        "is C521 AND container indicator is present (in 13/15) AND security is 0 AND Mode of transport is 4" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C521, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from MoreInformationPage to Check Your Answers when: " +
        "Place of loading is present AND container indicator is NOT captured in IE170 AND " +
        "is C523 AND container indicator is present (in 13/15) AND security is 0 AND Mode of transport is 4" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C523, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from Phone Number Page to Check Your Answers when: " +
        "Place of loading is present AND " +
        "Container indicator is not captured AND " +
        "Authorisation is not C521 AND " +
        "Transit Operation Security is not in {1, 2, 3} AND " +
        "Consignment Container Indicator is present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddPlaceOfLoadingYesNoPage, true)
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C523, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(containerIndicator = Some("indicator"))
                  )
                )

              navigator
                .nextPage(PhoneNumberPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from Phone Number Page to Check Your Answers when: " +
        "Place of loading is present AND " +
        "Container indicator is not captured AND " +
        "Authorisation is C521 AND " +
        "Transit Operation Limit Date is present AND " +
        "Transit Operation Security is not in {1, 2, 3} AND " +
        "Consignment Container Indicator is present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddPlaceOfLoadingYesNoPage, true)
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C521, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails, limitDate = Some("limitDate")),
                    Consignment = answers.departureData.Consignment.copy(containerIndicator = Some("indicator"))
                  )
                )

              navigator
                .nextPage(PhoneNumberPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from AddContactYesNoPage to Check Your Answers when: " +
        "the user selects No AND " +
        "Place of loading is present AND " +
        "Container indicator is not captured AND " +
        "Authorisation is not C521 AND " +
        "Transit Operation Security is not in {1, 2, 3} AND " +
        "Consignment Container Indicator is present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddContactYesNoPage, false)
                .setValue(AddPlaceOfLoadingYesNoPage, true)
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C523, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(containerIndicator = Some("indicator"))
                  )
                )

              navigator
                .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from AddContactYesNoPage to Check Your Answers when: " +
        "the user selects No AND " +
        "Place of loading is present AND " +
        "Container indicator is not captured AND " +
        "Authorisation is C521 AND " +
        "Transit Operation Limit Date is present AND " +
        "Transit Operation Security is not in {1, 2, 3} AND " +
        "Consignment Container Indicator is presentD" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(AddContactYesNoPage, false)
                .setValue(AddPlaceOfLoadingYesNoPage, true)
                .setValue(ContainerIndicatorPage, None)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "description"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Authorisation = Some(Seq(Authorisation(C521, "1234"))),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails, limitDate = Some("limitDate")),
                    Consignment = answers.departureData.Consignment.copy(containerIndicator = Some("indicator"))
                  )
                )

              navigator
                .nextPage(AddContactYesNoPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

      "must go from CustomsOfficeIdentifierPage to ContainerIdentificationNumberPage " +
        "when user hits 'Save and Continue', " +
        "consignment contains LocationOfGoods, " +
        "Security is NoSecurityDetails, " +
        "Container Indicator is true" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(ContainerIndicatorPage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails),
                    Consignment = answers.departureData.Consignment.copy(LocationOfGoods = Some(locationOfGoods))
                  )
                )

              navigator
                .nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))
          }
        }
    }

    "in Check Mode" - {

      "must go from limit date page to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(LimitDatePage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from LocationTypePage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(LocationTypePage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from IdentificationPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.setValue(IdentificationPage, LocationOfGoodsIdentification(PostalCodeIdentifier, s"$PostalCodeIdentifier - desc"))
            navigator
              .nextPage(IdentificationPage, updatedAnswers, departureId, CheckMode)
              .mustBe(controllers.locationOfGoods.routes.PostalCodeController.onPageLoad(departureId, CheckMode))
        }

      }

      "must go from AuthorisationNumberPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AuthorisationNumberPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from AddIdentifierYesNoPage" - {
        "to AdditionalIdentifierPage if answer is Yes and AdditionalIdentifierPage doesn't exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              (for {
                updatedAnswers                         <- answers.set(AddIdentifierYesNoPage, true)
                answersWithoutAdditionalIdentifierPage <- updatedAnswers.remove(AdditionalIdentifierPage)
              } yield navigator
                .nextPage(AddIdentifierYesNoPage, answersWithoutAdditionalIdentifierPage, departureId, CheckMode)
                .mustBe(controllers.locationOfGoods.routes.AdditionalIdentifierController.onPageLoad(departureId, CheckMode))).get
          }
        }
        //TODO talking to Ken this should follow on to other navigation
//        "to CYA page if answer is Yes but AdditionalIdentifierPage already exists" in {
//          forAll(arbitrary[UserAnswers]) {
//            answers =>
//              (for {
//                updatedAnswers                      <- answers.set(AddIdentifierYesNoPage, true)
//                answersWithAdditionalIdentifierPage <- updatedAnswers.set(AdditionalIdentifierPage, "identifier")
//              } yield navigator
//                .nextPage(AddIdentifierYesNoPage, answersWithAdditionalIdentifierPage, departureId, CheckMode)
//                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))).get
//          }
//        }
        "to CYA page if answer is No" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              answers
                .set(AddIdentifierYesNoPage, false)
                .map {
                  updatedAnswers =>
                    navigator
                      .nextPage(AddIdentifierYesNoPage, updatedAnswers, departureId, CheckMode)
                      .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
                }
                .get
          }
        }
      }

      "must go from AdditionalIdentifierPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AdditionalIdentifierPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from UnLocodePage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnLocodePage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from CustomsOfficeIdentifierPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(CustomsOfficeIdentifierPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from EoriPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EoriPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from CoordinatesPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(CoordinatesPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "when on AddContactYesNoPage" - {
        "must go from AddContactYesNoPage to check your answers page when no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val userAnswersSet = answers.setValue(AddContactYesNoPage, false)
              navigator
                .nextPage(AddContactYesNoPage, userAnswersSet, departureId, CheckMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

        }
        "must remove from userAnswers when AddContactYesNoPage is set to no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val userAnswersSet = answers.setValue(AddContactYesNoPage, false)

              userAnswersSet.get(CountryPage).mustBe(None)

              userAnswersSet.departureData.Consignment.LocationOfGoods.flatMap(_.Address) mustBe None

          }
        }

        "must go from AddContactYesNoPage to name page when yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val userAnswersSet = answers.setValue(AddContactYesNoPage, true)
              navigator
                .nextPage(AddContactYesNoPage, userAnswersSet, departureId, CheckMode)
                .mustBe(controllers.locationOfGoods.contact.routes.NameController.onPageLoad(departureId, CheckMode))
          }

        }
      }

      "must go from PhoneNumberPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(PhoneNumberPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from CountryPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(pages.locationOfGoods.CountryPage, answers, departureId, CheckMode)
              .mustBe(controllers.locationOfGoods.routes.AddressController.onPageLoad(departureId, CheckMode))
        }

      }

      "must go from AddressPage to check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AddressPage, answers, departureId, CheckMode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

    }
  }
}
