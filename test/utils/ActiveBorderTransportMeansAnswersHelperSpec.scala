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

import base.TestMessageData.{allOptionsNoneJsonValue, consignment}
import base.{SpecBase, TestMessageData}
import generators.Generators
import models.messages.MessageData
import models.reference.transport.border.active.Identification
import models.reference.{CustomsOffice, Nationality}
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active._
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ActiveBorderTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[CheckYourAnswersReferenceDataService]

  "ActiveBorderTransportMeansAnswersHelper" - {

    "addBorderMeansOfTransportYesNo" - {
      "must return No when BorderMeansOfTransport has not been answered in ie15/ie13" - {
        s"when $AddBorderMeansOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoAddBorderMeansOfTransportYesNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper =
                new ActiveBorderTransportMeansAnswersHelper(ie015WithNoAddBorderMeansOfTransportYesNoUserAnswers,
                                                            departureId,
                                                            refDataService,
                                                            mode,
                                                            activeIndex
                )
              val result = helper.addBorderMeansOfTransportYesNo.get

              result.key.value mustBe "Do you want to add identification for the border means of transport?"
              result.value.value mustBe "No"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add identification for the border means of transport"
              action.id mustBe "change-add-identification-for-the-border-means-of-transport"
          }
        }
      }

      "must return Yes when BorderMeansOfTransport has been answered in ie15/ie13" - {
        s"when $AddBorderMeansOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.addBorderMeansOfTransportYesNo.get

              result.key.value mustBe "Do you want to add identification for the border means of transport?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add identification for the border means of transport"
              action.id mustBe "change-add-identification-for-the-border-means-of-transport"
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
              val helper = new ActiveBorderTransportMeansAnswersHelper(noIdentificationTypeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.identificationType
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryIdentificationActive.arbitrary) {
            (mode, identification) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationPage(activeIndex), identification)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.identificationType) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Identification type"
                  result.value.value mustBe identification.asString
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, activeIndex).url
                  action.visuallyHiddenText.get mustBe "identification type for the border means of transport"
                  action.id mustBe "change-identification"
              }
          }
        }

        s"when $IdentificationPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val code = answers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.head.typeOfIdentification).get
              when(refDataService.getBorderMeansIdentification(any())(any())).thenReturn(Future.successful(Identification(code, "description")))
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.identificationType) {
                optionResult =>
                  val result = optionResult.get
                  result.key.value mustBe s"Identification type"
                  result.value.value mustBe messages(s"${Identification.messageKeyPrefix}.$code")
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, activeIndex).url
                  action.visuallyHiddenText.get mustBe "identification type for the border means of transport"
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
              val helper = new ActiveBorderTransportMeansAnswersHelper(noIdentificationTypeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.identificationNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              val result = helper.identificationNumber.get

              result.key.value mustBe s"Identification number"
              result.value.value mustBe identificationNumber
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.IdentificationNumberController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "identification number for the border means of transport"
              action.id mustBe "change-identification-number"
          }
        }

        s"when $IdentificationNumberPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)
              val result = helper.identificationNumber.get

              result.key.value mustBe s"Identification number"
              result.value.value mustBe "BX857GGE"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.IdentificationNumberController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "identification number for the border means of transport"
              action.id mustBe "change-identification-number"
          }
        }
      }
    }

    "nationality" - {
      "must return None when no nationality in ie13/ie15/170" - {
        s"when $NationalityPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noNationalityUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new ActiveBorderTransportMeansAnswersHelper(noNationalityUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.nationality
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $NationalityPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[Nationality]) {
            (mode, nationality) =>
              val answers = emptyUserAnswers
                .setValue(NationalityPage(activeIndex), nationality)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.nationality) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Registered country"
                  result.value.value mustBe nationality.description
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.NationalityController.onPageLoad(departureId, mode, activeIndex).url
                  action.visuallyHiddenText.get mustBe "registered country for the border means of transport"
                  action.id mustBe "change-nationality"
              }
          }
        }

        s"when $NationalityPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val code = answers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.head.nationality).get
              when(refDataService.getNationality(any())(any())).thenReturn(Future.successful(Nationality(code, "description")))
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)
              whenReady[Option[SummaryListRow], Assertion](helper.nationality) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Registered country"
                  result.value.value mustBe s"description"
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.NationalityController.onPageLoad(departureId, mode, activeIndex).url
                  action.visuallyHiddenText.get mustBe "registered country for the border means of transport"
                  action.id mustBe "change-nationality"
              }
          }
        }
      }
    }

    "customs office" - {
      "must return None when no customs office in ie13/ie15/170" - {
        s"when $CustomsOfficeActiveBorderPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noCustomsOfficeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new ActiveBorderTransportMeansAnswersHelper(noCustomsOfficeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.customsOffice
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CustomsOfficeActiveBorderPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              val answers = emptyUserAnswers
                .setValue(CustomsOfficeActiveBorderPage(activeIndex), customsOffice)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.customsOffice) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Customs office"
                  result.value.value mustBe customsOffice.toString
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.CustomsOfficeActiveBorderController
                    .onPageLoad(departureId, mode, activeIndex)
                    .url
                  action.visuallyHiddenText.get mustBe "customs office for the border means of transport"
                  action.id mustBe "change-customs-office"
              }
          }
        }

        s"when $CustomsOfficeActiveBorderPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val customOfficeId = answers.departureData.Consignment.ActiveBorderTransportMeans.get.head.customsOfficeAtBorderReferenceNumber.get
              when(refDataService.getCustomsOffice(any())(any()))
                .thenReturn(Future.successful(CustomsOffice(customOfficeId, "description", None)))
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              whenReady[Option[SummaryListRow], Assertion](helper.customsOffice) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Customs office"
                  result.value.value mustBe "description (GB000028)"
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.border.active.routes.CustomsOfficeActiveBorderController
                    .onPageLoad(departureId, mode, activeIndex)
                    .url
                  action.visuallyHiddenText.get mustBe "customs office for the border means of transport"
                  action.id mustBe "change-customs-office"
              }
          }
        }
      }
    }

    "conveyanceReferenceNumberYesNo" - {
      "IE170" - {
        "must return None when no conveyanceReferenceNumberYesNo" - {
          s"when $AddConveyanceReferenceYesNoPage undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val noConveyanceReferenceUserAnswers =
                  UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                val helper = new ActiveBorderTransportMeansAnswersHelper(noConveyanceReferenceUserAnswers, departureId, refDataService, mode, activeIndex)
                val result = helper.conveyanceReferenceNumberYesNo
                result mustBe None
            }
          }
        }

        "must return Some(Row)" - {
          s"when $AddConveyanceReferenceYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

                val result = helper.conveyanceReferenceNumberYesNo.get

                result.key.value mustBe "Do you want to add a conveyance reference number?"
                result.value.value mustBe "Yes"
                val actions = result.actions.get.items
                actions.size mustBe 1
                val action = actions.head
                action.content.value mustBe "Change"
                action.href mustBe controllers.transport.border.active.routes.AddConveyanceReferenceYesNoController
                  .onPageLoad(departureId, mode, activeIndex)
                  .url
                action.visuallyHiddenText.get mustBe "if you want to add a conveyance reference number"
                action.id mustBe "change-add-conveyance-reference-number"
            }
          }
        }
      }

      "IE13/15" - {
        "must return No when conveyanceReferenceNumber has not been provided" in {
          forAll(arbitrary[Mode], arbitraryActiveBorderTransportMeansWithoutConveyanceRefNo.arbitrary) {
            (mode, activeBorderTransportMeans) =>
              val noConveyanceReferenceNumberUserAnswers =
                emptyUserAnswers.copy(
                  departureData = TestMessageData.messageData.copy(
                    Consignment = consignment.copy(
                      ActiveBorderTransportMeans = activeBorderTransportMeans
                    )
                  )
                )

              val helper = new ActiveBorderTransportMeansAnswersHelper(noConveyanceReferenceNumberUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.conveyanceReferenceNumberYesNo.get

              result.key.value mustBe "Do you want to add a conveyance reference number?"
              result.value.value mustBe "No"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "if you want to add a conveyance reference number"
              action.id mustBe "change-add-conveyance-reference-number"
          }
        }

        "must return Yes when conveyanceReferenceNumber has been answered" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.conveyanceReferenceNumberYesNo.get

              result.key.value mustBe "Do you want to add a conveyance reference number?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "if you want to add a conveyance reference number"
              action.id mustBe "change-add-conveyance-reference-number"
          }
        }
      }
    }

    "conveyanceReferenceNumber" - {
      "must return None when no conveyanceReferenceNumber in ie13/ie15/170" - {
        s"when $ConveyanceReferenceNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noIdentificationTypeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new ActiveBorderTransportMeansAnswersHelper(noIdentificationTypeUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.conveyanceReferenceNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ConveyanceReferenceNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, conveyanceReferenceNumber) =>
              val answers = emptyUserAnswers
                .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceReferenceNumber)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)

              val result = helper.conveyanceReferenceNumber.get

              result.key.value mustBe s"Conveyance reference number"
              result.value.value mustBe conveyanceReferenceNumber
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "conveyance reference number for the border means of transport"
              action.id mustBe "change-conveyance-reference-number"
          }
        }

        s"when $IdentificationNumberPage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, refDataService, mode, activeIndex)
              val result = helper.conveyanceReferenceNumber.get

              result.key.value mustBe s"Conveyance reference number"
              result.value.value mustBe "REF2"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.active.routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustBe "conveyance reference number for the border means of transport"
              action.id mustBe "change-conveyance-reference-number"
          }
        }
      }
    }
  }
}
