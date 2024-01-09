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

package utils

import base.SpecBase
import base.TestMessageData.{allOptionsNoneJsonValue, messageData}
import config.Constants._
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generators.Generators
import models.messages.{Address, MessageData}
import models.reference.{Country, CountryCode, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, Mode, PostalCodeAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import viewModels.Section

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationOfGoodsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]

  val identifications: Seq[LocationOfGoodsIdentification] = Seq(
    LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier"),
    LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier"),
    LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber"),
    LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier"),
    LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
    LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier"),
    LocationOfGoodsIdentification(PostalCodeIdentifier, "PostalCode")
  )

  val identificationsExcludingAddress: Seq[LocationOfGoodsIdentification] = Seq(
    LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier"),
    LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber"),
    LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier"),
    LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
    LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier"),
    LocationOfGoodsIdentification(PostalCodeIdentifier, "PostalCode")
  )

  "LocationOfGoodsAnswersHelper" - {

    "when Location of Goods is present in IE170" - {
      "locationType" - {
        "must return Some(Row)" - {
          s"when LocationTypePage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[LocationType]) {
              (mode, locationType) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(LocationTypePage, locationType)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.locationTypeRow(locationType.toString).get

                result.key.value mustBe "Location type"
                result.value.value mustBe locationType.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "location type"
                action.id mustBe "change-location-type"
            }
          }
        }
      }

      "qualifierIdentification" - {
        "must return Some(Row)" - {
          s"when IdentificationPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[LocationOfGoodsIdentification]) {
              (mode, identification) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(IdentificationPage, identification)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.qualifierIdentificationRow(identification.description).get

                result.key.value mustBe "Identifier type"
                result.value.value mustBe identification.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "identifier type for the location of goods"
                action.id mustBe "change-qualifier-identification"
            }
          }
        }
      }

      "authorisationNumber" - {
        "must return Some(Row)" - {
          s"when AuthorisationNumberPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, authorisationNumber) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AuthorisationNumberPage, authorisationNumber)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.authorisationNumber

                result.get.key.value mustBe "Authorisation number"
                result.get.value.value mustBe authorisationNumber
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the authorisation number for the location of goods"
                action.id mustBe "change-authorisation-number"
            }
          }
        }
      }

      "eori" - {
        "must return Some(Row)" - {
          "when eori defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAuthorisationNumberUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                  = new LocationOfGoodsAnswersHelper(ie015WithAuthorisationNumberUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                  = helper.eoriNumber

                result.get.key.value mustBe "EORI number or Trader Identification Number (TIN)"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.get.EconomicOperator.get.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.EoriController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "EORI number or Trader Identification Number (TIN) for the location of goods"
                action.id mustBe "change-eori"
            }
          }
          s"when EoriPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, locationOfGoodsAnswerEori) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(EoriPage, locationOfGoodsAnswerEori)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.eoriNumber

                result.get.key.value mustBe "EORI number or Trader Identification Number (TIN)"
                result.get.value.value mustBe locationOfGoodsAnswerEori
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.EoriController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "EORI number or Trader Identification Number (TIN) for the location of goods"
                action.id mustBe "change-eori"
            }
          }
        }

        "must return None" - {
          "when EORI undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.eoriNumber
                result mustBe None
            }
          }
        }
      }

      "additionalIdentifierYesNo" - {
        "must return Some(Row)" - {
          s"when AddIdentifierYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Boolean]) {
              (mode, additionalIdentifier) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AddIdentifierYesNoPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifierYesNo

                result.get.key.value mustBe "Do you want to add an additional identifier for the location of goods?"
                result.get.value.value mustBe (if (additionalIdentifier) "Yes" else "No")
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "if you want to add another identifier for the location of goods"
                action.id mustBe "change-add-additional-identifier"
            }
          }
        }
      }

      "additionalIdentifier" - {
        "must return Some(Row)" - {
          s"when AddIdentifierYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, additionalIdentifier) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AddIdentifierYesNoPage, true)
                  .setValue(AdditionalIdentifierPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifierRow

                result.get.key.value mustBe "Additional identifier"
                result.get.value.value mustBe additionalIdentifier
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AdditionalIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "additional identifier"
                action.id mustBe "change-additional-identifier"
            }
          }
        }
      }

      "unLocode" - {
        "must return Some(Row)" - {
          s"when UnLocodePage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, unLocode) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(UnLocodePage, unLocode)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.unLocode

                result.get.key.value mustBe "UN/LOCODE"
                result.get.value.value mustBe unLocode
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "UN/LOCODE for the location of goods"
                action.id mustBe "change-unLocode"
            }
          }
        }
      }

      "Customs Office" - {

        "Customs Office identifier row" - {
          "must return Some(Row) when CustomsOfficeIdentifierPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
              (mode, customsOffice) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(CustomsOfficeIdentifierPage, customsOffice)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.customsOfficeIdentifierRow(customsOffice.toString)

                result.key.value mustBe "Customs office identifier"
                result.value.value mustBe customsOffice.toString
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the customs office identifier for the location of goods"
                action.id mustBe "change-customs-office-identifier"
            }
          }
        }
      }

      "coordinates" - {

        "must return None" - {
          "when coordinates is undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.coordinates
                result mustBe None
            }
          }
        }

        "must return Some(Row)" - {
          s"when CoordinatesPage is defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Coordinates]) {
              (mode, coordinates) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(CoordinatesPage, coordinates)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.coordinates

                result.get.key.value mustBe "Coordinates"
                result.get.value.value mustBe coordinates.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CoordinatesController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "coordinates for the location of goods"
                action.id mustBe "change-coordinates"
            }
          }
        }
      }

      "locationOfGoodsContactYesNo" - {
        "must return Some(Row)" - {
          "when locationOfGoodsContactYesNo defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAcontactPageUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                           = new LocationOfGoodsAnswersHelper(ie015WithAcontactPageUserAnswers, departureId, mockReferenceDataService, mode)
                val result                           = helper.locationOfGoodsContactYesNo

                result.get.key.value mustBe "Do you want to add a contact for the location of goods?"
                result.get.value.value mustBe "Yes"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddContactYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "if you want to add a contact for the location of goods"
                action.id mustBe "change-add-contact"
            }
          }
          s"when locationOfGoodsContactYesNo defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AddContactYesNoPage, true)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactYesNo

                result.get.key.value mustBe "Do you want to add a contact for the location of goods?"
                result.get.value.value mustBe "Yes"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddContactYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "if you want to add a contact for the location of goods"
                action.id mustBe "change-add-contact"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactYesNo undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactYesNo
                result mustBe None
            }
          }
        }
      }

      "locationOfGoodsContactPersonName" - {
        "must return Some(Row)" - {
          "when locationOfGoodsContactYesNo defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithlocationOfGoodsContactPersonNameUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper = new LocationOfGoodsAnswersHelper(ie015WithlocationOfGoodsContactPersonNameUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonName

                result.get.key.value mustBe "Contact’s name"
                result.get.value.value mustBe "Paul Sully"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.contact.routes.NameController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the contact for the location of goods"
                action.id mustBe "change-person-name"
            }
          }
          s"when locationOfGoodsContactPersonName defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(NamePage, "Han Solo")
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonName

                result.get.key.value mustBe "Contact’s name"
                result.get.value.value mustBe "Han Solo"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.contact.routes.NameController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the contact for the location of goods"
                action.id mustBe "change-person-name"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactPersonName undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonName
                result mustBe None
            }
          }
        }
      }

      "locationOfGoodsContactPersonNumber" - {
        "must return Some(Row)" - {
          "when locationOfGoodsContactPersonNumber defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithlocationOfGoodsContactPersonNumberUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper =
                  new LocationOfGoodsAnswersHelper(ie015WithlocationOfGoodsContactPersonNumberUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonNumber

                result.get.key.value mustBe "Contact’s phone number"
                result.get.value.value mustBe "07508994566"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.contact.routes.PhoneNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "contact’s phone number for the location of goods"
                action.id mustBe "change-person-number"
            }
          }
          s"when locationOfGoodsContactPersonNumber defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(PhoneNumberPage, "999")
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonNumber

                result.get.key.value mustBe "Contact’s phone number"
                result.get.value.value mustBe "999"
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.contact.routes.PhoneNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "contact’s phone number for the location of goods"
                action.id mustBe "change-person-number"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactPersonNumber undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationOfGoodsContactPersonNumber
                result mustBe None
            }
          }
        }
      }

      "country" - {
        "must return Some(Row)" - {
          "when country defined in ie15" in {
            forAll(arbitrary[Mode], arbitrary[Country]) {
              (mode, countryType) =>
                when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(countryType))

                val ie015UserAnswers = UserAnswers(
                  departureId,
                  eoriNumber,
                  lrn.value,
                  Json.obj(),
                  Instant.now(),
                  messageData.copy(Consignment =
                    messageData.Consignment.copy(LocationOfGoods =
                      Some(
                        messageData.Consignment.LocationOfGoods.get
                          .copy(Address = Some(Address(streetAndNumber = "", postcode = None, city = "", country = countryType.code.code)))
                      )
                    )
                  )
                )
                val helper = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.countryTypeRow(countryType.description).get

                result.key.value mustBe "Country"
                result.value.value mustBe countryType.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CountryController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "country for the location of goods"
                action.id mustBe "change-location-of-goods-country"
            }
          }
          s"when CountryPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Country]) {
              (mode, countryType) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(CountryPage, countryType)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.countryTypeRow(countryType.description).get

                result.key.value mustBe "Country"
                result.value.value mustBe countryType.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CountryController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "country for the location of goods"
                action.id mustBe "change-location-of-goods-country"
            }
          }
        }
      }

      "address" - {
        "must return Some(Row)" - {
          "when address defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAddressUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper =
                  new LocationOfGoodsAnswersHelper(ie015WithAddressUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.address

                result.get.key.value mustBe "Address"
                result.get.value.value mustBe ie015WithAddressUserAnswers.departureData.Consignment.LocationOfGoods
                  .flatMap(_.Address.map(_.toDynamicAddress))
                  .get
                  .toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddressController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "address for the location of goods"
                action.id mustBe "change-location-of-goods-address"
            }
          }
          s"when address defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[DynamicAddress]) {
              (mode, addressData) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AddressPage, addressData)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.address

                result.get.key.value mustBe "Address"
                result.get.value.value mustBe addressData.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddressController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "address for the location of goods"
                action.id mustBe "change-location-of-goods-address"
            }
          }
        }

        "must return None" - {
          "when address undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.address
                result mustBe None
            }
          }
        }
      }

      "postCodeAddress" - {
        "must return Some(Row)" - {
          "when postCodeAddress defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAddressUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper =
                  new LocationOfGoodsAnswersHelper(ie015WithAddressUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.postCodeAddress

                result.get.key.value mustBe "Postal code"
                result.get.value.value mustBe ie015WithAddressUserAnswers.departureData.Consignment.LocationOfGoods
                  .flatMap(_.PostcodeAddress.map(_.toPostalCode))
                  .get
                  .toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.PostalCodeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the address for the location of goods"
                action.id mustBe "change-location-of-goods-postalCode"
            }
          }
          s"when postCodeAddress defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[PostalCodeAddress]) {
              (mode, addressData) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(PostalCodePage, addressData)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.postCodeAddress

                result.get.key.value mustBe "Postal code"
                result.get.value.value mustBe addressData.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.PostalCodeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the address for the location of goods"
                action.id mustBe "change-location-of-goods-postalCode"
            }
          }
        }

        "must return None" - {
          "when postCodeAddress undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.postCodeAddress
                result mustBe None
            }
          }
        }
      }

      "section should contain all the answer rows" in {
        forAll(arbitrary[Mode], arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification], arbitrary[String], arbitrary[Boolean], arbitrary[String]) {
          (mode, locationType, identification, authorisationNumber, additionalIdentifierYesNo, additionalIdentifier) =>
            val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(LocationTypePage, locationType)
              .setValue(IdentificationPage, identification)
              .setValue(AuthorisationNumberPage, authorisationNumber)
              .setValue(AddIdentifierYesNoPage, additionalIdentifierYesNo)
              .setValue(AdditionalIdentifierPage, additionalIdentifier)
              .setValue(UnLocodePage, "unLocode")
              .setValue(CustomsOfficeIdentifierPage, CustomsOffice("id", "name", None))
              .setValue(AddContactYesNoPage, true)
              .setValue(PhoneNumberPage, "999")
              .setValue(NamePage, "Han Solo")
            val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)

            whenReady[Section, Assertion](helper.locationOfGoodsSection) {
              section =>
                section.rows.size mustBe 10
            }
        }
      }
    }

    "when Location of Goods is NOT present in IE170 and is present in departure data (IE13/15)" - {
      "locationType" - {
        "future must return a failure" - {
          s"when reference data call fails to find the code" in {

            forAll(arbitrary[Mode]) {
              mode =>
                when(mockReferenceDataService.getLocationType(any())(any())).thenReturn(
                  Future.failed(new NoReferenceDataFoundException)
                )
                val answers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData]).copy(departureData =
                    emptyUserAnswers.departureData.copy(Consignment =
                      emptyUserAnswers.departureData.Consignment.copy(LocationOfGoods =
                        emptyUserAnswers.departureData.Consignment.LocationOfGoods.map(
                          locationOfGoods => locationOfGoods.copy(typeOfLocation = "loc")
                        )
                      )
                    )
                  )

                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.fetchLocationTypeRow(answers)

                whenReady[Throwable, Assertion](result.failed) {
                  _ mustBe a[NoReferenceDataFoundException]
                }

            }
          }
        }
        "must return Some(Row)" - {
          "when locationType defined in ie15" in {

            forAll(arbitrary[Mode], Gen.oneOf(locationTypes)) {
              (mode, locationType) =>
                when(mockReferenceDataService.getLocationType(any())(any())).thenReturn(Future.successful(locationType))

                val ie015UserAnswers = UserAnswers(
                  departureId,
                  eoriNumber,
                  lrn.value,
                  Json.obj(),
                  Instant.now(),
                  messageData.copy(Consignment =
                    messageData.Consignment.copy(LocationOfGoods = Some(messageData.Consignment.LocationOfGoods.get.copy(typeOfLocation = locationType.`type`)))
                  )
                )
                val helper = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.locationTypeRow(locationType.description).get

                result.key.value mustBe "Location type"
                result.value.value mustBe locationType.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "location type"
                action.id mustBe "change-location-type"
            }
          }
        }
      }

      "qualifierIdentification" - {
        "future must return a failure" - {
          s"when reference data call fails to find the code" in {

            forAll(arbitrary[Mode]) {
              mode =>
                when(mockReferenceDataService.getQualifierOfIdentification(any())(any())).thenReturn(
                  Future.failed(new NoReferenceDataFoundException)
                )
                val answers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData]).copy(departureData =
                    emptyUserAnswers.departureData.copy(Consignment =
                      emptyUserAnswers.departureData.Consignment.copy(LocationOfGoods =
                        emptyUserAnswers.departureData.Consignment.LocationOfGoods.map(
                          locationOfGoods => locationOfGoods.copy(qualifierOfIdentification = "qual")
                        )
                      )
                    )
                  )

                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.fetchQualifierIdentificationRow(emptyUserAnswers)

                whenReady[Throwable, Assertion](result.failed) {
                  _ mustBe a[NoReferenceDataFoundException]
                }

            }
          }
        }
        "must return Some(Row)" - {
          "when qualifierIdentification defined in ie15 and is not FreeText/Address" in {
            forAll(arbitrary[Mode], Gen.oneOf(identificationsExcludingAddress)) {
              (mode, identification) =>
                when(mockReferenceDataService.getQualifierOfIdentification(any())(any())).thenReturn(Future.successful(identification))

                val data = messageData.copy(Consignment =
                  messageData.Consignment.copy(LocationOfGoods =
                    Some(messageData.Consignment.LocationOfGoods.get.copy(qualifierOfIdentification = identification.description))
                  )
                )
                val ie015UserAnswers = UserAnswers(
                  departureId,
                  eoriNumber,
                  lrn.value,
                  Json.obj(),
                  Instant.now(),
                  data
                )

                val helper = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.qualifierIdentificationRow(identification.description).get

                result.key.value mustBe "Identifier type"
                result.value.value mustBe identification.description
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "identifier type for the location of goods"
                action.id mustBe "change-qualifier-identification"
            }
          }
          "when qualifierIdentification defined in ie15 and is FreeText/Address" in {
            forAll(arbitrary[Mode]) {
              mode =>
                when(mockReferenceDataService.getQualifierOfIdentification(any())(any()))
                  .thenReturn(Future.successful(LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier")))

                val data = messageData.copy(Consignment =
                  messageData.Consignment.copy(LocationOfGoods = Some(messageData.Consignment.LocationOfGoods.get.copy(qualifierOfIdentification = "Address")))
                )
                val ie015UserAnswers = UserAnswers(
                  departureId,
                  eoriNumber,
                  lrn.value,
                  Json.obj(),
                  Instant.now(),
                  data
                )

                val helper = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.qualifierIdentificationRow("AddressIdentifier").get

                result.key.value mustBe "Identifier type"
                result.value.value mustBe "AddressIdentifier"
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "identifier type for the location of goods"
                action.id mustBe "change-qualifier-identification"
            }
          }

        }
      }

      "authorisationNUmber" - {

        "must return None" - {
          "when authorisationNumber undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.authorisationNumber
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when authorisationNumber defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAuthorisationNumberUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                  = new LocationOfGoodsAnswersHelper(ie015WithAuthorisationNumberUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                  = helper.authorisationNumber

                result.get.key.value mustBe "Authorisation number"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.get.authorisationNumber.get
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the authorisation number for the location of goods"
                action.id mustBe "change-authorisation-number"
            }
          }
        }
      }

      "additionalIdentifierYesNo" - {

        "must return None" - {
          "when additionalIdentifier undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifierYesNo
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when additional identifier defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAdditionalIdentifierUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                   = new LocationOfGoodsAnswersHelper(ie015WithAdditionalIdentifierUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                   = helper.additionalIdentifierYesNo

                result.get.key.value mustBe "Do you want to add an additional identifier for the location of goods?"
                result.get.value.value mustBe (if (messageData.Consignment.LocationOfGoods.exists(_.additionalIdentifier.isDefined)) "Yes" else "No")
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "if you want to add another identifier for the location of goods"
                action.id mustBe "change-add-additional-identifier"
            }
          }
        }
      }

      "additionalIdentifier" - {

        "must return None" - {
          "when additionalIdentifier undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifierRow
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when additional identifier defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAdditionalIdentifierUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                   = new LocationOfGoodsAnswersHelper(ie015WithAdditionalIdentifierUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                   = helper.additionalIdentifierRow

                result.get.key.value mustBe "Additional identifier"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.get.additionalIdentifier.get
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AdditionalIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "additional identifier"
                action.id mustBe "change-additional-identifier"
            }
          }
        }
      }

      "unLocode" - {

        "must return None" - {
          "when unLocode undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.unLocode
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when unLocode defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015UserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper           = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result           = helper.unLocode

                result.get.key.value mustBe "UN/LOCODE"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.get.UNLocode.get
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "UN/LOCODE for the location of goods"
                action.id mustBe "change-unLocode"
            }
          }
        }
      }

      "customsOfficeIdentifier" - {
        "future must return a failure" - {
          s"when reference data call fails to find the code" in {

            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, customsOfficeId) =>
                when(mockReferenceDataService.getCustomsOffice(any())(any())).thenReturn(
                  Future.failed(new NoReferenceDataFoundException)
                )
                val answers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData]).copy(departureData =
                    emptyUserAnswers.departureData.copy(Consignment =
                      emptyUserAnswers.departureData.Consignment.copy(LocationOfGoods =
                        emptyUserAnswers.departureData.Consignment.LocationOfGoods.map(
                          locationOfGoods => locationOfGoods.copy(CustomsOffice = Some(models.messages.CustomsOffice(customsOfficeId)))
                        )
                      )
                    )
                  )

                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.fetchCustomsOfficeIdentifierRow(emptyUserAnswers)

                whenReady[Throwable, Assertion](result.failed) {
                  _ mustBe a[NoReferenceDataFoundException]
                }

            }
          }
        }
        "must return Some(Row)" - {
          "when customs office identifier defined in ie15" in {
            forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
              (mode, customsOffice) =>
                when(mockReferenceDataService.getCustomsOffice(any())(any())).thenReturn(Future.successful(customsOffice))

                val ie015UserAnswers = UserAnswers(
                  departureId,
                  eoriNumber,
                  lrn.value,
                  Json.obj(),
                  Instant.now(),
                  messageData.copy(Consignment =
                    messageData.Consignment.copy(LocationOfGoods =
                      Some(messageData.Consignment.LocationOfGoods.get.copy(CustomsOffice = Some(models.messages.CustomsOffice(customsOffice.id))))
                    )
                  )
                )
                val helper = new LocationOfGoodsAnswersHelper(ie015UserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.customsOfficeIdentifierRow(customsOffice.toString)

                result.key.value mustBe "Customs office identifier"
                result.value.value mustBe customsOffice.toString
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "the customs office identifier for the location of goods"
                action.id mustBe "change-customs-office-identifier"
            }
          }
        }
      }

      "coordinates" - {

        "must return None" - {
          "when coordinates is undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.coordinates
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when coordinates is defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAdditionalIdentifierUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                   = new LocationOfGoodsAnswersHelper(ie015WithAdditionalIdentifierUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                   = helper.coordinates

                result.get.key.value mustBe "Coordinates"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.flatMap(_.GNSS).get.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.CoordinatesController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "coordinates for the location of goods"
                action.id mustBe "change-coordinates"
            }
          }
        }
      }

      "section should contain all the answer rows" in {
        when(mockReferenceDataService.getLocationType(any())(any())).thenReturn(Future.successful(LocationType("A", "a desc")))
        when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(Country(CountryCode("GB"), "United Kingdom")))
        when(mockReferenceDataService.getQualifierOfIdentification(any())(any()))
          .thenReturn(Future.successful(LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier")))
        when(mockReferenceDataService.getCustomsOffice(any())(any()))
          .thenReturn(Future.successful(CustomsOffice("id", "name", None)))
        forAll(arbitrary[Mode]) {
          mode =>
            val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
            val helper  = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)

            whenReady[Section, Assertion](helper.locationOfGoodsSection) {
              section =>
                section.rows.size mustBe 15
            }
        }
      }
    }
  }
}
