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
import config.Constants.QualifierOfTheIdentification._
import generators.Generators
import models.reference.{Country, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import services.CheckYourAnswersReferenceDataService

class LocationOfGoodsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

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
                val answers = emptyUserAnswers
                  .setValue(IdentificationPage, identification)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.qualifierIdentificationRow.get

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
                val answers = emptyUserAnswers
                  .setValue(AuthorisationNumberPage, authorisationNumber)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
          s"when EoriPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[String]) {
              (mode, locationOfGoodsAnswerEori) =>
                val answers = emptyUserAnswers
                  .setValue(EoriPage, locationOfGoodsAnswerEori)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
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
                val answers = emptyUserAnswers
                  .setValue(AddIdentifierYesNoPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val answers = emptyUserAnswers
                  .setValue(AddIdentifierYesNoPage, true)
                  .setValue(AdditionalIdentifierPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val answers = emptyUserAnswers
                  .setValue(UnLocodePage, unLocode)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val answers = emptyUserAnswers
                  .setValue(CustomsOfficeIdentifierPage, customsOffice)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
                val result = helper.customsOfficeIdentifierRow.get

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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.coordinates
                result mustBe None
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
          s"when locationOfGoodsContactYesNo defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddContactYesNoPage, true)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactYesNo
                result mustBe None
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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonName
                result mustBe None
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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.locationOfGoodsContactPersonNumber
                result mustBe None
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
          s"when address defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[DynamicAddress]) {
              (mode, addressData) =>
                val answers = emptyUserAnswers
                  .setValue(AddressPage, addressData)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mode)
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
                val helper = new LocationOfGoodsAnswersHelper(emptyUserAnswers, departureId, mode)
                val result = helper.address
                result mustBe None
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

            helper.locationOfGoodsSection.rows.size mustBe 10
        }
      }
    }

  }
}
