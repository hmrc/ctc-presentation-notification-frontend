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
import generators.Generators
import models.messages.MessageData
import models.{Coordinates, LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods._
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import services.CheckYourAnswersReferenceDataService.ReferenceDataNotFoundException

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
                val result = helper.locationTypeRow(locationType.toString)

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
                val result = helper.qualifierIdentificationRow(identification.toString)

                result.key.value mustBe "Identifier type for the location of goods"
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

                result.get.key.value mustBe "EORI number or TIN for the location of goods"
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

                result.get.key.value mustBe "EORI number or TIN for the location of goods"
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

      "additionalIdentifier" - {
        "must return Some(Row)" - {
          s"when AddIdentifierYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode], arbitrary[Boolean]) {
              (mode, additionalIdentifier) =>
                val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                  .setValue(AddIdentifierYesNoPage, additionalIdentifier)
                val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifier

                result.get.key.value mustBe "Additional identifier"
                result.get.value.value mustBe additionalIdentifier.toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "additional identifier"
                action.id mustBe "change-additional-identifier"
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

      "section should contain all the answer rows" in {
        forAll(arbitrary[Mode], arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification], arbitrary[String], arbitrary[Boolean]) {
          (mode, locationType, identification, authorisationNumber, additionalIdentifier) =>
            val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(LocationTypePage, locationType)
              .setValue(IdentificationPage, identification)
              .setValue(AuthorisationNumberPage, authorisationNumber)
              .setValue(AddIdentifierYesNoPage, additionalIdentifier)
            val helper = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)

            whenReady(helper.locationOfGoodsSection) {
              section =>
                section.rows.size mustBe 4
            }
        }
      }
    }

    "when Location of Goods is NOT present in IE170 and is present in departure data (IE13/15)" - {
      "locationType" - {
        "future must return a failure" - {
          s"when reference data call fails to find the code" in {

            forAll(arbitrary[Mode], arbitrary[LocationType]) {
              (mode, location) =>
                val referenceDataNotFoundException =
                  new ReferenceDataNotFoundException(refName = "locationType", refDataCode = location.code, listRefData = Nil)
                when(mockReferenceDataService.getBorderMode(any())(any())).thenReturn(
                  Future.failed(referenceDataNotFoundException)
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

                val helper = new PresentationNotificationAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.borderModeSection

                whenReady[Throwable, Assertion](result.failed) {
                  _ mustBe referenceDataNotFoundException
                }

            }
          }

        }
        "must return Some(Row)" - {
          "when LocationTypePage defined in ie15" in {
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
                val result = helper.locationTypeRow(locationType.description)

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

            forAll(arbitrary[Mode], arbitrary[LocationOfGoodsIdentification]) {
              (mode, qualifier) =>
                val referenceDataNotFoundException =
                  new ReferenceDataNotFoundException(refName = "qualifierOfIdentification", refDataCode = qualifier.code, listRefData = Nil)
                when(mockReferenceDataService.getBorderMode(any())(any())).thenReturn(
                  Future.failed(referenceDataNotFoundException)
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

                val helper = new PresentationNotificationAnswersHelper(answers, departureId, mockReferenceDataService, mode)
                val result = helper.borderModeSection

                whenReady[Throwable, Assertion](result.failed) {
                  _ mustBe referenceDataNotFoundException
                }

            }
          }
        }
        "must return Some(Row)" - {
          "when IdentificationPage defined in ie15" in {
            forAll(arbitrary[Mode], Gen.oneOf(identifications)) {
              (mode, identification) =>
                when(mockReferenceDataService.getQualifierOfIdentification(any())(any())).thenReturn(Future.successful(identification))

                val data = messageData.copy(Consignment =
                  messageData.Consignment.copy(LocationOfGoods =
                    Some(messageData.Consignment.LocationOfGoods.get.copy(qualifierOfIdentification = identification.qualifier))
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
                val result = helper.qualifierIdentificationRow(identification.toString)

                result.key.value mustBe "Identifier type for the location of goods"
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
          "when AuthorisationNumberPage defined in ie15" in {
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

      "additionalIdentifier" - {

        "must return None" - {
          "when additionalIdentifier undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithNoUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new LocationOfGoodsAnswersHelper(ie015WithNoUserAnswers, departureId, mockReferenceDataService, mode)
                val result = helper.additionalIdentifier
                result mustBe None
            }
          }
        }
        "must return Some(Row)" - {
          "when AddIdentifierYesNoPage defined in ie15" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val ie015WithAdditionalIdentifierUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
                val helper                                   = new LocationOfGoodsAnswersHelper(ie015WithAdditionalIdentifierUserAnswers, departureId, mockReferenceDataService, mode)
                val result                                   = helper.additionalIdentifier

                result.get.key.value mustBe "Additional identifier"
                result.get.value.value mustBe messageData.Consignment.LocationOfGoods.exists(_.additionalIdentifier.isDefined).toString
                val actions = result.get.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, mode).url
                action.visuallyHiddenText.get mustBe "additional identifier"
                action.id mustBe "change-additional-identifier"
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
        forAll(arbitrary[Mode]) {
          mode =>
            val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
            val helper  = new LocationOfGoodsAnswersHelper(answers, departureId, mockReferenceDataService, mode)

            whenReady(helper.locationOfGoodsSection) {
              section =>
                section.rows.size mustBe 6
            }
        }
      }
    }
  }
}
