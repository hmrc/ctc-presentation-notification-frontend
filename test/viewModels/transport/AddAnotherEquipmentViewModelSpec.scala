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

package viewModels.transport

import base.SpecBase
import controllers.transport.equipment.routes
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.AddTransportEquipmentYesNoPage
import pages.transport.equipment._
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import viewModels.ListItem
import viewModels.transport.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider

class AddAnotherEquipmentViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherEquipmentViewModelSpec" - {
    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode)
              result.listItems mustBe Nil
          }
        }
      }

      "when user answers populated with one equipment and container id" - {
        "and at index 0 and add equipment yes/no page is true" - {
          "must return one list item with remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(AddTransportEquipmentYesNoPage, true)
                  .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = "#", //TODO to be implemented as part of CTCP-4057
                    removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(userAnswers.lrn, mode, equipmentIndex).url)
                  )
                )
            }
          }

          "must return one list item with no remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                val result      = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = "#", //TODO to be implemented as part of CTCP-4057
                    removeUrl = None
                  )
                )
            }
          }
        }
      }

      "when user answers populated with one equipment and without container id" - {
        "must return one list item" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers
                .setValue(AddTransportEquipmentYesNoPage, true)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode)

              result.listItems.length mustBe 1
              result.title mustBe "You have added 1 transport equipment"
              result.heading mustBe "You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = s"Transport equipment 1 - no container identification number",
                  changeUrl = "#", //TODO to be implemented as part of CTCP-4057
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(userAnswers.lrn, mode, Index(0)).url)
                )
              )
          }
        }
      }

      "when user answers is populated with more than one equipment" - {
        "must return multiple list items" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, containerId) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode)

              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = "#", //TODO to be implemented as part of CTCP-4057
                  removeUrl = None
                ),
                ListItem(
                  name = s"Transport equipment 2 - container $containerId",
                  changeUrl = "#", //TODO to be implemented as part of CTCP-4057
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(userAnswers.lrn, mode, Index(1)).url)
                )
              )
          }
        }
      }
    }
  }
}
