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
import generated._
import generators.Generators
import models.{Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.{AddInlandModeOfTransportYesNoPage, InlandModePage}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import viewModels.Section

class InlandModeAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "InlandModeAnswersHelper" - {

    "addInlandModeOfTransportYesNo" - {
      "must return No" - {
        s"when $AddInlandModeOfTransportYesNoPage is false" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(AddInlandModeOfTransportYesNoPage, false)
              val helper =
                new InlandModeAnswersHelper(userAnswers, departureId, mode)
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

      "must return Yes" - {
        s"when $AddInlandModeOfTransportYesNoPage is true" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val updatedAnswers = userAnswers.setValue(AddInlandModeOfTransportYesNoPage, true)

              val helper = new InlandModeAnswersHelper(updatedAnswers, departureId, mode)
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
      "must return None" - {
        s"when $InlandModePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new InlandModeAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.inlandMode
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $InlandModePage defined" in {
          forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary) {
            (mode, inlandMode) =>
              val answers = emptyUserAnswers
                .setValue(InlandModePage, inlandMode)
              val helper = new InlandModeAnswersHelper(answers, departureId, mode)

              val result = helper.inlandMode.get

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

    }

    "buildInlandModeSection" - {
      "must return None when reduced data set indicator is true" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val departureData = basicIe015.copy(TransitOperation = basicIe015.TransitOperation.copy(reducedDatasetIndicator = Number1))
            val helper        = new InlandModeAnswersHelper(emptyUserAnswers.copy(departureData = departureData), departureId, mode)
            val result        = helper.buildInlandModeSection
            result mustBe None
        }
      }

      "must return Some(Section()) when reduced data set indicator is false" in {
        forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary) {
          (mode, inlandMode) =>
            val departureData = basicIe015.copy(TransitOperation = basicIe015.TransitOperation.copy(reducedDatasetIndicator = Number0))

            val answers = emptyUserAnswers
              .copy(departureData = departureData)
              .setValue(InlandModePage, inlandMode)
              .setValue(AddInlandModeOfTransportYesNoPage, true)

            val helper = new InlandModeAnswersHelper(answers, departureId, mode)
            val result = helper.buildInlandModeSection

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
