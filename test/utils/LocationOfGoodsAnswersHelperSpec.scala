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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.Constants.QualifierOfTheIdentification.*
import generators.Generators
import models.reference.{Country, CustomsOffice, LocationType}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, Mode}
import org.scalacheck.Arbitrary.arbitrary
import pages.locationOfGoods.*
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import services.CheckYourAnswersReferenceDataService

class LocationOfGoodsAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]

  val identifications: Seq[LocationOfGoodsIdentification] = Seq(
    LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier"),
    LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier"),
    LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber"),
    LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier"),
    LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
    LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier")
  )

  val identificationsExcludingAddress: Seq[LocationOfGoodsIdentification] = Seq(
    LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier"),
    LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber"),
    LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier"),
    LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
    LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier")
  )

  "LocationOfGoodsAnswersHelper" - {

    "when Location of Goods is present in IE170" - {
      "locationType" - {
        "must return Some(Row)" - {
          s"when LocationTypePage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[LocationType]) {
              (mode, locationType) =>
                val answers = emptyUserAnswers
                  .setValue(LocationTypePage, locationType)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.locationTypeRow.get

                result.key.value mustEqual "Location type"
                result.value.value mustEqual locationType.description
                val actions = result.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "location type"
                action.id mustEqual "change-location-type"
            }
          }
        }
      }

      "qualifierIdentification" - {
        "must return Some(Row)" - {
          s"when IdentificationPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[LocationOfGoodsIdentification]) {
              (mode, identification) =>
                val answers = emptyUserAnswers
                  .setValue(IdentificationPage, identification)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.qualifierIdentificationRow.get

                result.key.value mustEqual "Identifier type"
                result.value.value mustEqual identification.description
                val actions = result.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "identifier type for the location of goods"
                action.id mustEqual "change-qualifier-identification"
            }
          }
        }
      }

      "authorisationNumber" - {
        "must return Some(Row)" - {
          s"when AuthorisationNumberPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, authorisationNumber) =>
                val answers = emptyUserAnswers
                  .setValue(AuthorisationNumberPage, authorisationNumber)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.authorisationNumber

                result.get.key.value mustEqual "Authorisation number"
                result.get.value.value mustEqual authorisationNumber
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "the authorisation number for the location of goods"
                action.id mustEqual "change-authorisation-number"
            }
          }
        }
      }

      "eori" - {
        "must return Some(Row)" - {
          s"when EoriPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, locationOfGoodsAnswerEori) =>
                val answers = emptyUserAnswers
                  .setValue(EoriPage, locationOfGoodsAnswerEori)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.eoriNumber

                result.get.key.value mustEqual "EORI number or Trader Identification Number (TIN)"
                result.get.value.value mustEqual locationOfGoodsAnswerEori
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.EoriController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "EORI number or Trader Identification Number (TIN) for the location of goods"
                action.id mustEqual "change-eori"
            }
          }
        }

        "must return None" - {
          "when EORI undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.eoriNumber
                result must not be defined
            }
          }
        }
      }

      "additionalIdentifierYesNo" - {
        "must return Some(Row)" - {
          s"when AddIdentifierYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Boolean]) {
              (mode, additionalIdentifier) =>
                val answers = emptyUserAnswers
                  .setValue(AddIdentifierYesNoPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.additionalIdentifierYesNo

                result.get.key.value mustEqual "Do you want to add an additional identifier for the location of goods?"
                result.get.value.value mustEqual (if (additionalIdentifier) "Yes" else "No")
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "if you want to add an additional identifier for the location of goods"
                action.id mustEqual "change-add-additional-identifier"
            }
          }
        }
      }

      "additionalIdentifier" - {
        "must return Some(Row)" - {
          s"when AddIdentifierYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, additionalIdentifier) =>
                val answers = emptyUserAnswers
                  .setValue(AddIdentifierYesNoPage, true)
                  .setValue(AdditionalIdentifierPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.additionalIdentifierRow

                result.get.key.value mustEqual "Additional identifier"
                result.get.value.value mustEqual additionalIdentifier
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.AdditionalIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "additional identifier"
                action.id mustEqual "change-additional-identifier"
            }
          }
        }
      }

      "unLocode" - {
        "must return Some(Row)" - {
          s"when UnLocodePage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, unLocode) =>
                val answers = emptyUserAnswers
                  .setValue(UnLocodePage, unLocode)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.unLocode

                result.get.key.value mustEqual "UN/LOCODE"
                result.get.value.value mustEqual unLocode
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "UN/LOCODE for the location of goods"
                action.id mustEqual "change-unLocode"
            }
          }
        }
      }

      "Customs Office" - {

        "Customs Office identifier row" - {
          "must return Some(Row) when CustomsOfficeIdentifierPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
              (mode, customsOffice) =>
                val answers = emptyUserAnswers
                  .setValue(CustomsOfficeIdentifierPage, customsOffice)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.customsOfficeIdentifierRow.get

                result.key.value mustEqual "Customs office identifier"
                result.value.value mustEqual customsOffice.toString
                val actions = result.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "the customs office identifier for the location of goods"
                action.id mustEqual "change-customs-office-identifier"
            }
          }
        }
      }

      "coordinates" - {

        "must return None" - {
          "when coordinates is undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.coordinates
                result must not be defined
            }
          }
        }

        "must return Some(Row)" - {
          s"when CoordinatesPage is defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Coordinates]) {
              (mode, coordinates) =>
                val answers = emptyUserAnswers
                  .setValue(CoordinatesPage, coordinates)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.coordinates

                result.get.key.value mustEqual "Coordinates"
                result.get.value.value mustEqual coordinates.toString
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.CoordinatesController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "coordinates for the location of goods"
                action.id mustEqual "change-coordinates"
            }
          }
        }
      }

      "locationOfGoodsContactYesNo" - {
        "must return Some(Row)" - {
          s"when locationOfGoodsContactYesNo defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddContactYesNoPage, true)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.locationOfGoodsContactYesNo

                result.get.key.value mustEqual "Do you want to add a contact for the location of goods?"
                result.get.value.value mustEqual "Yes"
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.AddContactYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "if you want to add a contact for the location of goods"
                action.id mustEqual "change-add-contact"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactYesNo undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactYesNo
                result must not be defined
            }
          }
        }
      }

      "locationOfGoodsContactPersonName" - {
        "must return Some(Row)" - {
          s"when locationOfGoodsContactPersonName defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(NamePage, "Han Solo")
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonName

                result.get.key.value mustEqual "Contact’s name"
                result.get.value.value mustEqual "Han Solo"
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.contact.routes.NameController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "the contact for the location of goods"
                action.id mustEqual "change-person-name"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactPersonName undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonName
                result must not be defined
            }
          }
        }
      }

      "locationOfGoodsContactPersonNumber" - {
        "must return Some(Row)" - {
          s"when locationOfGoodsContactPersonNumber defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(PhoneNumberPage, "999")
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonNumber

                result.get.key.value mustEqual "Contact’s phone number"
                result.get.value.value mustEqual "999"
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.contact.routes.PhoneNumberController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "contact’s phone number for the location of goods"
                action.id mustEqual "change-person-number"
            }
          }
        }

        "must return None" - {
          "when locationOfGoodsContactPersonNumber undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonNumber
                result must not be defined
            }
          }
        }
      }

      "country" - {
        "must return Some(Row)" - {
          s"when CountryPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Country]) {
              (mode, countryType) =>
                val answers = emptyUserAnswers
                  .setValue(CountryPage, countryType)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.countryTypeRow.get

                result.key.value mustEqual "Country"
                result.value.value mustEqual countryType.description
                val actions = result.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.CountryController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "country for the location of goods"
                action.id mustEqual "change-location-of-goods-country"
            }
          }
        }
      }

      "address" - {
        "must return Some(Row)" - {
          s"when address defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[DynamicAddress]) {
              (mode, addressData) =>
                val answers = emptyUserAnswers
                  .setValue(AddressPage, addressData)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.address

                result.get.key.value mustEqual "Address"
                result.get.value.value mustEqual addressData.toString
                val actions = result.get.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.locationOfGoods.routes.AddressController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustEqual "address for the location of goods"
                action.id mustEqual "change-location-of-goods-address"
            }
          }
        }

        "must return None" - {
          "when address undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.address
                result must not be defined
            }
          }
        }
      }

      "section should contain all the answer rows" in {
        forAll(arbitrary[Mode], arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification], arbitrary[String], arbitrary[Boolean], arbitrary[String]) {
          (mode, locationType, identification, authorisationNumber, additionalIdentifierYesNo, additionalIdentifier) =>
            val answers = emptyUserAnswers
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
            val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)

            helper.locationOfGoodsSection.rows.size mustEqual 10
        }
      }
    }

  }
}
