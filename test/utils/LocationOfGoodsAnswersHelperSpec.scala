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
import models.{LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.{AuthorisationNumberPage, IdentificationPage, LocationTypePage}
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService

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
    "locationType" - {
      "must return Some(Row)" - {
        s"when LocationTypePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[LocationType]) {
            (mode, locationType) =>
              when(mockReferenceDataService.getLocationType(any())(any())).thenReturn(Future.successful(Some(locationType)))

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

        "when LocationTypePage defined in ie15" in {
          forAll(arbitrary[Mode], Gen.oneOf(locationTypes)) {
            (mode, locationType) =>
              when(mockReferenceDataService.getLocationType(any())(any())).thenReturn(Future.successful(Some(locationType)))

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

      "must return Some(Row)" - {
        s"when IdentificationPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[LocationOfGoodsIdentification]) {
            (mode, identification) =>
              when(mockReferenceDataService.getQualifierOfIdentification(any())(any())).thenReturn(Future.successful(Some(identification)))

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

        "when IdentificationPage defined in ie15" in {
          forAll(arbitrary[Mode], Gen.oneOf(identifications)) {
            (mode, identification) =>
              when(mockReferenceDataService.getQualifierOfIdentification(any())(any())).thenReturn(Future.successful(Some(identification)))

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
  }
}
