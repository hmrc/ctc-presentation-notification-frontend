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
import base.TestMessageData.{allOptionsNoneJsonValue, allOptionsNoneReducedDatasetTrueJsonValue}
import generators.Generators
import models.messages.MessageData
import models.reference.TransportMode.InlandMode
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.{AddInlandModeOfTransportYesNoPage, InlandModePage}
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import viewModels.Section

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InlandModeAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[CheckYourAnswersReferenceDataService]

  "InlandModeAnswersHelper" - {

    "addInlandModeOfTransportYesNo" - {
      "must return No when AddInlandModeOfTransportYesNo has not been answered in ie15/ie13" - {
        s"when $AddInlandModeOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoAddInlandModeOfTransportYesNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper =
                new InlandModeAnswersHelper(ie015WithNoAddInlandModeOfTransportYesNoUserAnswers, departureId, refDataService, mode)
              val result = helper.inlandModeOfTransportYesNo.get

              result.key.value mustBe "Do you want to add an inland mode of transport?"
              result.value.value mustBe "No"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.AddInlandModeOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add an inland mode of transport"
              action.id mustBe "change-add-inland-mode-of-transport"
          }
        }
      }

      "must return Yes when AddInlandModeOfTransportYesNo has been answered in ie15/ie13" - {
        s"when $AddInlandModeOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val helper = new InlandModeAnswersHelper(userAnswers, departureId, refDataService, mode)
              val result = helper.inlandModeOfTransportYesNo.get

              result.key.value mustBe "Do you want to add an inland mode of transport?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.AddInlandModeOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add an inland mode of transport"
              action.id mustBe "change-add-inland-mode-of-transport"
          }
        }
      }
    }

    "inlandMode" - {
      "must return None when no inlandMode type in ie13/ie15/170" - {
        s"when $InlandModePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val noInlandModeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new InlandModeAnswersHelper(noInlandModeUserAnswers, departureId, refDataService, mode)
              val result = helper.inlandMode
              result.futureValue mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $InlandModePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary) {
            (mode, inlandMode) =>
              val answers = emptyUserAnswers
                .setValue(InlandModePage, inlandMode)
              val helper = new InlandModeAnswersHelper(answers, departureId, refDataService, mode)

              whenReady[Option[SummaryListRow], Assertion](helper.inlandMode) {
                optionResult =>
                  val result = optionResult.get

                  result.key.value mustBe s"Mode"
                  result.value.value mustBe inlandMode.asString
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode).url
                  action.visuallyHiddenText.get mustBe "inland mode of transport"
                  action.id mustBe "change-transport-inland-mode"
              }
          }
        }

        s"when $InlandModePage defined in the ie13/15" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, answers) =>
              val code = answers.departureData.Consignment.inlandModeOfTransport.get
              when(refDataService.getInlandModeOfTransport(any())(any())).thenReturn(Future.successful(InlandMode(code, "description")))
              val helper = new InlandModeAnswersHelper(answers, departureId, refDataService, mode)

              whenReady[Option[SummaryListRow], Assertion](helper.inlandMode) {
                optionResult =>
                  val result = optionResult.get
                  result.key.value mustBe s"Mode"
                  result.value.value mustBe "description"
                  val actions = result.actions.get.items
                  actions.size mustBe 1
                  val action = actions.head
                  action.content.value mustBe "Change"
                  action.href mustBe controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode).url
                  action.visuallyHiddenText.get mustBe "inland mode of transport"
                  action.id mustBe "change-transport-inland-mode"
              }
          }
        }
      }
    }

    "buildInlandModeSection" - {
      "must return None when reduced data set indicator is true" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val ie015withReducedDataSetFalseUserAnswers =
              UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneReducedDatasetTrueJsonValue.as[MessageData])
            val helper =
              new InlandModeAnswersHelper(ie015withReducedDataSetFalseUserAnswers, departureId, refDataService, mode)
            val result = helper.buildInlandModeSection.futureValue
            result mustBe None
        }
      }

      "must return Some(Section()) when reduced data set indicator is false" in {
        forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary) {
          (mode, inlandMode) =>
            val answers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)

            val helper =
              new InlandModeAnswersHelper(answers, departureId, refDataService, mode)
            val result = helper.buildInlandModeSection.futureValue

            val inlandModeYesNoRow = SummaryListRow(
              key = Key(Text("Do you want to add an inland mode of transport?")),
              value = Value(Text("Yes")),
              actions = Some(
                Actions(items =
                  Seq(
                    ActionItem(
                      href = controllers.transport.routes.AddInlandModeOfTransportYesNoController.onPageLoad(departureId, mode).url,
                      visuallyHiddenText = Some("if you want to add an inland mode of transport"),
                      content = Text("Change"),
                      attributes = Map("id" -> "change-add-inland-mode-of-transport")
                    )
                  )
                )
              )
            )

            val inlandModeRow = SummaryListRow(
              key = Key(Text("Mode")),
              value = Value(Text(inlandMode.description)),
              actions = Some(
                Actions(items =
                  Seq(
                    ActionItem(
                      href = controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode).url,
                      visuallyHiddenText = Some("inland mode of transport"),
                      content = Text("Change"),
                      attributes = Map("id" -> "change-transport-inland-mode")
                    )
                  )
                )
              )
            )

            val section = Section(
              sectionTitle = "Inland mode of transport",
              rows = Seq(
                inlandModeYesNoRow,
                inlandModeRow
              )
            )

            result.get mustBe section
            result.get.rows.size mustBe 2
        }
      }

    }
  }
}
