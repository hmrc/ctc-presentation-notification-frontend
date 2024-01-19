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
import base.TestMessageData.allOptionsNoneJsonValue
import generators.Generators
import models.messages.MessageData
import models.reference.Nationality
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import pages.houseConsignment.index.departureTransportMeans._
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HouseConsignmentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[CheckYourAnswersReferenceDataService]

  "HouseConsignmentAnswersHelper" - {

    "addDepartureTransportMeansYesNo" - {
      "must return No when DepartureTransportMeans has not been answered in ie15/ie13" - {
        s"when ${AddDepartureTransportMeansYesNoPage(houseConsignmentIndex)} undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoHCDepartureTransportMeansUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper =
                new HouseConsignmentAnswersHelper(ie015WithNoHCDepartureTransportMeansUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.addDepartureTransportMeansYesNo.get

              result.key.value mustBe "Do you want to add a departure means of transport for house consignment 1?"
              result.value.value mustBe "No"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.houseConsignment.index.routes.AddDepartureTransportMeansYesNoController
                .onPageLoad(departureId, mode, houseConsignmentIndex)
                .url
              action.visuallyHiddenText.get mustBe "if you want to add identification for the departure transport means"
              action.id mustBe "change-add-departure-means-of-transport"
          }
        }
      }

      "must return Yes when DepartureTransportMeans has been answered in ie15/ie13" - {
        s"when ${AddDepartureTransportMeansYesNoPage(houseConsignmentIndex)} undefined" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val helper = new HouseConsignmentAnswersHelper(userAnswers, departureId, refDataService, mode, houseConsignmentIndex)
              val result = helper.addDepartureTransportMeansYesNo.get

              result.key.value mustBe "Do you want to add a departure means of transport for house consignment 1?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.houseConsignment.index.routes.AddDepartureTransportMeansYesNoController
                .onPageLoad(departureId, mode, houseConsignmentIndex)
                .url
              action.visuallyHiddenText.get mustBe "if you want to add identification for the departure transport means"
              action.id mustBe "change-add-departure-means-of-transport"
          }
        }
      }
    }

    "identificationType" - {
      "must return None when no identification type in ie13/ie15/170" - {
        s"when $IdentificationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noIdentificationTypeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new HouseConsignmentAnswersHelper(noIdentificationTypeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.identificationType(houseConsignmentDepartureTransportMeansIndex)
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryTransportMeansIdentification.arbitrary) {
            (mode, identification) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identification)
              val helper = new HouseConsignmentAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.identificationType(houseConsignmentDepartureTransportMeansIndex)) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Identification type"
                  result.value.value mustBe identification.asString
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.houseConsignment.index.departureTransportMeans.routes.IdentificationController
                    .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
                    .url
                  action.visuallyHiddenText.get mustBe "identification type for the departure means of transport for house consignment 1"
                  action.id mustBe "change-identification"
              }
          }
        }
      }
    }

    "identificationNumber" - {
      "must return None when no identification number in ie13/ie15/170" - {
        s"when $IdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noIdentificationTypeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new HouseConsignmentAnswersHelper(noIdentificationTypeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.identificationNumber(houseConsignmentDepartureTransportMeansIndex)
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identificationNumber)
              val helper = new HouseConsignmentAnswersHelper(answers, departureId, refDataService, mode, houseConsignmentIndex)

              val result = helper.identificationNumber(houseConsignmentDepartureTransportMeansIndex).get

              result.key.value mustBe s"Identification number"
              result.value.value mustBe identificationNumber
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.houseConsignment.index.departureTransportMeans.routes.IdentificationNumberController
                .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
                .url
              action.visuallyHiddenText.get mustBe "identification number for the departure means of transport for house consignment 1"
              action.id mustBe "change-identification-number"
          }
        }

        s"when $IdentificationNumberPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val helper = new HouseConsignmentAnswersHelper(answers, departureId, refDataService, mode, houseConsignmentIndex)
              val result = helper.identificationNumber(houseConsignmentIndex).get

              result.key.value mustBe s"Identification number"
              result.value.value mustBe "BX857GGE"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.houseConsignment.index.departureTransportMeans.routes.IdentificationNumberController
                .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
                .url
              action.visuallyHiddenText.get mustBe "identification number for the departure means of transport for house consignment 1"
              action.id mustBe "change-identification-number"
          }
        }
      }
    }

    "nationality" - {
      "must return None when no nationality in ie13/ie15/170" - {
        s"when $CountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noNationalityUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new HouseConsignmentAnswersHelper(noNationalityUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.nationality(houseConsignmentDepartureTransportMeansIndex)
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CountryPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[Nationality]) {
            (mode, nationality) =>
              val answers = emptyUserAnswers
                .setValue(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), nationality)
              val helper = new HouseConsignmentAnswersHelper(answers, departureId, refDataService, mode, houseConsignmentIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.nationality(houseConsignmentDepartureTransportMeansIndex)) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Registered country"
                  result.value.value mustBe nationality.description
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.houseConsignment.index.departureTransportMeans.routes.CountryController
                    .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
                    .url
                  action.visuallyHiddenText.get mustBe "registered country for the departure means of transport for house consignment 1"
                  action.id mustBe "change-country"
              }
          }
        }

        s"when $CountryPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val country = answers.departureData.Consignment.HouseConsignment
                .lift(houseConsignmentIndex.position)
                .flatMap(
                  _.DepartureTransportMeans.flatMap(
                    seq => seq.lift(houseConsignmentDepartureTransportMeansIndex.position).flatMap(_.nationality)
                  )
                )
                .get
              when(refDataService.getNationality(any())(any())).thenReturn(Future.successful(Nationality(country, "description")))
              val helper = new HouseConsignmentAnswersHelper(answers, departureId, refDataService, mode, houseConsignmentIndex)
              whenReady[Option[SummaryListRow], Assertion](helper.nationality(houseConsignmentDepartureTransportMeansIndex)) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Registered country"
                  result.value.value mustBe s"description"
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.houseConsignment.index.departureTransportMeans.routes.CountryController
                    .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
                    .url
                  action.visuallyHiddenText.get mustBe "registered country for the departure means of transport for house consignment 1"
                  action.id mustBe "change-country"
              }
          }
        }
      }
    }
  }
}
