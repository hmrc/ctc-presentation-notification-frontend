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

package viewModels.transport.equipment

import base.SpecBase
import config.Constants.AuthorisationTypeDeparture.{ACR, SSE}
import controllers.transport.equipment.index.routes
import generated.AuthorisationType03
import generators.Generators
import models.reference.Item
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.index._
import pages.transport.equipment.{AddTransportEquipmentYesNoPage, ItemPage}
import viewModels.ListItem
import viewModels.transport.equipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider

class AddAnotherEquipmentViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AddAnotherEquipmentViewModelSpec" - {
    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = true)
              result.listItems mustBe Nil
          }
        }
      }

      "remove links" - {
        "should show when" - {
          "container indicator is true and" - {
            "equipments > 1" - {
              "must return two list item with remove link" in {
                forAll(arbitrary[Mode], nonEmptyString) {
                  (mode, containerId) =>
                    val userAnswers = emptyUserAnswers
                      .setValue(ContainerIndicatorPage, true)
                      .setValue(AddTransportEquipmentYesNoPage, false)
                      .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                      .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                      .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                      .setValue(AddContainerIdentificationNumberYesNoPage(Index(1)), true)

                    val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                    result.listItems.length mustBe 2

                    result.listItems mustBe Seq(
                      ListItem(
                        name = s"Transport equipment 1 - container $containerId",
                        changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                          .onPageLoad(departureId, mode, equipmentIndex)
                          .url,
                        removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                      ),
                      ListItem(
                        name = s"Transport equipment 2 - container $containerId",
                        changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                          .onPageLoad(departureId, mode, Index(1))
                          .url,
                        removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(1)).url)
                      )
                    )
                }
              }
            }
          }

          "container indicator is false" - {
            "must return two list item with remove link" in {
              forAll(arbitrary[Mode], nonEmptyString) {
                (mode, containerId) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(ContainerIndicatorPage, false)
                    .setValue(AddTransportEquipmentYesNoPage, true)
                    .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                    .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                    .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                    .setValue(AddContainerIdentificationNumberYesNoPage(Index(1)), true)
                  val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                  result.listItems.length mustBe 2

                  result.listItems mustBe Seq(
                    ListItem(
                      name = s"Transport equipment 1 - container $containerId",
                      changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController
                        .onPageLoad(departureId, mode, equipmentIndex)
                        .url,
                      removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                    ),
                    ListItem(
                      name = s"Transport equipment 2 - container $containerId",
                      changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController
                        .onPageLoad(departureId, mode, Index(1))
                        .url,
                      removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(1)).url)
                    )
                  )
              }
            }
          }
        }

        "should not show" - {
          "when container indicator is true" - {
            "and equipments length == 1" in {
              forAll(arbitrary[Mode], nonEmptyString) {
                (mode, containerId) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(ContainerIndicatorPage, true)
                    .setValue(AddTransportEquipmentYesNoPage, false)
                    .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                    .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)

                  val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                  result.listItems.length mustBe 1

                  result.listItems mustBe Seq(
                    ListItem(
                      name = s"Transport equipment 1 - container $containerId",
                      changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                        .onPageLoad(departureId, mode, equipmentIndex)
                        .url,
                      removeUrl = None
                    )
                  )
              }
            }
          }
        }
      }

      "when user answers populated with one equipment and container id" - {
        "and at index 0 and add equipment yes/no page is false" - {
          "must return one list item without remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, true)
                  .setValue(AddTransportEquipmentYesNoPage, false)
                  .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                  .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                      .onPageLoad(departureId, mode, equipmentIndex)
                      .url,
                    removeUrl = None
                  )
                )
            }
          }

          "must return one list item with no remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, true)
                  .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                      .onPageLoad(departureId, mode, equipmentIndex)
                      .url,
                    removeUrl = None
                  )
                )
            }
          }
        }
      }

      "when user answers populated with one equipment and container id" - {
        "and at index 0 and add equipment yes/no page is true" - {
          "must return one list item without remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, true)
                  .setValue(AddTransportEquipmentYesNoPage, true)
                  .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                      .onPageLoad(departureId, mode, equipmentIndex)
                      .url,
                    removeUrl = None
                  )
                )
            }
          }

          "must return one list item with no remove link" in {
            forAll(arbitrary[Mode], nonEmptyString) {
              (mode, containerId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, true)
                  .setValue(ContainerIdentificationNumberPage(Index(0)), containerId)
                val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

                result.listItems.length mustBe 1
                result.title mustBe "You have added 1 transport equipment"
                result.heading mustBe "You have added 1 transport equipment"
                result.legend mustBe "Do you want to add any other transport equipment?"
                result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

                result.listItems mustBe Seq(
                  ListItem(
                    name = s"Transport equipment 1 - container $containerId",
                    changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                      .onPageLoad(departureId, mode, equipmentIndex)
                      .url,
                    removeUrl = None
                  )
                )
            }
          }
        }
      }

      "when user answers populated with one equipment and without container id" - {
        "must return one list item" in {
          forAll(arbitrary[Mode], arbitrary[Item]) {
            (mode, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddTransportEquipmentYesNoPage, true)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), true)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ContainerIndicatorPage, true)
                .setValue(ItemPage(Index(0), Index(0)), item)
              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 1
              result.title mustBe "You have added 1 transport equipment"
              result.heading mustBe "You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = s"Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex)
                    .url,
                  removeUrl = None
                )
              )
          }
        }
      }

      "when user answers is populated with more than one equipment" - {
        "must return multiple list items  when container indicator is true" in {
          forAll(arbitrary[Mode], nonEmptyString, arbitrary[Item]) {
            (mode, containerId, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                .setValue(ItemPage(Index(0), Index(0)), item)

              val result = new AddAnotherEquipmentViewModelProvider().apply(userAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex)
                    .url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"Transport equipment 2 - container $containerId",
                  changeUrl = controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

        "must return multiple list items when container indicator is false and Authorisation is C521 and C523 change url must point to AddContainerIdentificationNumberYesNoController," in {
          forAll(arbitrary[Mode], nonEmptyString, arbitrary[Item]) {
            (mode, containerId, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, false)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                .setValue(ItemPage(Index(0), Index(0)), item)

              val updatedUserAnswers =
                userAnswers.copy(departureData =
                  basicIe015.copy(Authorisation = Seq(AuthorisationType03(BigInt(1), SSE, "1234"), AuthorisationType03(BigInt(2), ACR, "1234")))
                )

              val result = new AddAnotherEquipmentViewModelProvider().apply(updatedUserAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, sealIndex)
                    .url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"Transport equipment 2 - container $containerId",
                  changeUrl = controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, Index(1), sealIndex)
                    .url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

        "must return multiple list items when container indicator is false and Authorisation is C521 but not C523, change link must redirect to AddSealsController" in {
          forAll(arbitrary[Mode], nonEmptyString, arbitrary[Item]) {
            (mode, containerId, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, false)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ContainerIdentificationNumberPage(Index(1)), containerId)
                .setValue(ItemPage(Index(0), Index(0)), item)

              val updatedUserAnswers =
                userAnswers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03(BigInt(1), ACR, "1234"))))

              val result = new AddAnotherEquipmentViewModelProvider().apply(updatedUserAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 2
              result.title mustBe s"You have added 2 transport equipment"
              result.heading mustBe s"You have added 2 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                    .url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"Transport equipment 2 - container $containerId",
                  changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

        "must not show remove link when there is only 1 equipment and the section is mandatory(there is no answer to do you want to add an equipment page)" in {
          forAll(arbitrary[Mode], arbitrary[Item]) {
            (mode, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(ItemPage(Index(0), Index(0)), item)

              val updatedUserAnswers =
                userAnswers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03(BigInt(1), ACR, "1234"))))

              val result = new AddAnotherEquipmentViewModelProvider().apply(updatedUserAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 1
              result.title mustBe s"You have added 1 transport equipment"
              result.heading mustBe s"You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                    .url,
                  removeUrl = None
                )
              )
          }
        }

        "must show remove link when there is only 1 equipment and the section is optional (there is a yes answer to do you want to add an equipment page and container indicator is false)" in {
          forAll(arbitrary[Mode], arbitrary[Item]) {
            (mode, item) =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, false)
                .setValue(AddContainerIdentificationNumberYesNoPage(Index(0)), false)
                .setValue(AddSealYesNoPage(Index(0)), false)
                .setValue(AddTransportEquipmentYesNoPage, true)
                .setValue(ItemPage(Index(0), Index(0)), item)

              val updatedUserAnswers =
                userAnswers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03(BigInt(1), ACR, "1234"))))

              val result = new AddAnotherEquipmentViewModelProvider().apply(updatedUserAnswers, departureId, mode, isNumberItemsZero = false)

              result.listItems.length mustBe 1
              result.title mustBe s"You have added 1 transport equipment"
              result.heading mustBe s"You have added 1 transport equipment"
              result.legend mustBe "Do you want to add any other transport equipment?"
              result.maxLimitLabel mustBe "You cannot add any more transport equipment. To add another, you need to remove one first."

              result.listItems mustBe Seq(
                ListItem(
                  name = "Transport equipment 1 - no container identification number",
                  changeUrl = controllers.transport.equipment.index.routes.AddSealYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                    .url,
                  removeUrl = Some(routes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, Index(0)).url)
                )
              )
          }
        }

      }
    }
  }
}
