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
import base.TestMessageData.{allOptionsNoneJsonValue, consignment, jsonValue}
import generators.Generators
import models.messages.MessageData
import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans._
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[CheckYourAnswersReferenceDataService]

  "DepartureTransportMeansAnswersHelper" - {

    "identification number row" - {
      "must return None when no identification number in ie13/ie15/170" - {
        s"when $TransportMeansIdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new DepartureTransportMeansAnswersHelper(userAnswers, departureId, refDataService, mode, transportIndex)
              val result = helper.identificationNumberRow
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $TransportMeansIdentificationNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, number) =>
              val answers = emptyUserAnswers
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), number)
              val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)

              val result = helper.identificationNumberRow.get

              result.key.value mustBe "Identification number"
              result.value.value mustBe number
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController
                .onPageLoad(departureId, mode, transportIndex)
                .url
              action.visuallyHiddenText.get mustBe "the identification number for the departure means of transport"
              action.id mustBe "change-departure-transport-means-identification-number"
          }
        }
      }

      s"when $InlandModePage defined in the ie13/15" in {
        forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
          (mode, answers) =>
            val number = answers.departureData.Consignment.DepartureTransportMeans.flatMap(_.identificationNumber).get
            val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
            val result = helper.identificationNumberRow.get

            result.key.value mustBe "Identification number"
            result.value.value mustBe number
            val actions = result.actions.get.items
            actions.size mustBe 1
            val action = actions.head
            action.content.value mustBe "Change"
            action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController
              .onPageLoad(departureId, mode, transportIndex)
              .url
            action.visuallyHiddenText.get mustBe "the identification number for the departure means of transport"
            action.id mustBe "change-departure-transport-means-identification-number"
        }
      }
    }
  }

  "identification type row" - {
    "must return None when no identification type in ie13/ie15/170" - {
      s"when $TransportMeansIdentificationPage undefined" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val userAnswers =
              UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
            val helper = new DepartureTransportMeansAnswersHelper(userAnswers, departureId, refDataService, mode, transportIndex)
            val result = helper.identificationType
            result.futureValue mustBe None
        }
      }
    }

    "must return Some(Row)" - {
      s"when $TransportMeansIdentificationPage defined in the ie170" in {
        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification]) {
          (mode, means) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(transportIndex), means)

            val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
            whenReady[Option[SummaryListRow], Assertion](helper.identificationType) {
              optionResult =>
                val result = optionResult.get

                result.key.value mustBe "Identification type"
                result.value.value mustBe means.toString
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController
                  .onPageLoad(departureId, mode, transportIndex)
                  .url
                action.visuallyHiddenText.get mustBe "identification type for the departure means of transport"
                action.id mustBe "change-transport-means-identification"
            }
        }
      }
    }

    s"when $TransportMeansIdentificationPage defined in the ie13/15" in {
      forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
        (mode, answers) =>
          val identificationType = answers.departureData.Consignment.DepartureTransportMeans.flatMap(_.typeOfIdentification).get

          when(refDataService.getMeansOfTransportIdentificationType(any())(any()))
            .thenReturn(Future.successful(TransportMeansIdentification(identificationType, "description")))

          val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
          whenReady[Option[SummaryListRow], Assertion](helper.identificationType) {
            optionResult =>
              val result = optionResult.get

              result.key.value mustBe "Identification type"
              result.value.value mustBe "description"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController
                .onPageLoad(departureId, mode, transportIndex)
                .url
              action.visuallyHiddenText.get mustBe "identification type for the departure means of transport"
              action.id mustBe "change-transport-means-identification"
          }
      }
    }
  }
  "nationality type row" - {
    "must return None when no nationality in ie13/ie15/170" - {
      s"when $TransportMeansNationalityPage undefined" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val userAnswers =
              UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
            val helper = new DepartureTransportMeansAnswersHelper(userAnswers, departureId, refDataService, mode, transportIndex)
            val result = helper.nationality
            result.futureValue mustBe None
        }
      }
    }

    "must return Some(Row)" - {
      s"when $TransportMeansNationalityPage defined in the ie170" in {
        forAll(arbitrary[Mode], arbitrary[Nationality]) {
          (mode, nationality) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansNationalityPage(transportIndex), nationality)

            val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
            whenReady[Option[SummaryListRow], Assertion](helper.nationality) {
              optionResult =>
                val result = optionResult.get

                result.key.value mustBe "Registered country"
                result.value.value mustBe nationality.toString
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController
                  .onPageLoad(departureId, mode, transportIndex)
                  .url
                action.visuallyHiddenText.get mustBe "registered country for the departure means of transport"
                action.id mustBe "change-departure-transport-means-nationality"
            }
        }
      }
    }

    s"when $TransportMeansNationalityPage defined in the ie13/15" in {
      forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
        (mode, answers) =>
          val nationalityCode = answers.departureData.Consignment.DepartureTransportMeans.flatMap(_.nationality).get
          val nationality     = Nationality(nationalityCode, "description")

          when(refDataService.getNationality(any())(any()))
            .thenReturn(Future.successful(nationality))

          val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
          whenReady[Option[SummaryListRow], Assertion](helper.nationality) {
            optionResult =>
              val result = optionResult.get

              result.key.value mustBe "Registered country"
              result.value.value mustBe nationality.toString
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController
                .onPageLoad(departureId, mode, transportIndex)
                .url
              action.visuallyHiddenText.get mustBe "registered country for the departure means of transport"
              action.id mustBe "change-departure-transport-means-nationality"
          }
      }
    }
  }

  "buildDepartureTransportMeansSection" - {
    "must return None inland mode is 5" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val jsonData = jsonValue.as[MessageData].copy(Consignment = consignment.copy(inlandModeOfTransport = Some("5")))
          val ie015withReducedDataSetFalseUserAnswers =
            UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), jsonData)
          val helper =
            new DepartureTransportMeansAnswersHelper(ie015withReducedDataSetFalseUserAnswers, departureId, refDataService, mode, transportIndex)
          val result = helper.buildDepartureTransportMeansSection.futureValue
          result mustBe None
      }
    }

    "must return Some(Section()) when inland mode is not 5 " in {
      forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary.suchThat(_.isNotOneOf("5"))) {
        (mode, inlandMode) =>
          val answers = emptyUserAnswers
            .setValue(InlandModePage, inlandMode)

          val helper =
            new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
          val result = helper.buildDepartureTransportMeansSection.futureValue

          result.get.rows.size mustBe 3
          result.get.sectionTitle.get mustBe "Departure means of transport"
      }
    }

    "must return Some(Section()) when inland mode is not present" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val answers = emptyUserAnswers

          val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, refDataService, mode, transportIndex)
          val result = helper.buildDepartureTransportMeansSection.futureValue

          result.get.rows.size mustBe 3
          result.get.sectionTitle.get mustBe "Departure means of transport"
      }
    }

  }

}
