/*
 * Copyright 2024 HM Revenue & Customs
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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.Item
import models.{Index, Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.sections.transport.equipment.{EquipmentSection, ItemSection, SealSection}
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.index.{AddContainerIdentificationNumberYesNoPage, AddSealYesNoPage, ContainerIdentificationNumberPage}
import pages.transport.equipment.{AddTransportEquipmentYesNoPage, ItemPage}
import play.api.libs.json.Json

class TransportEquipmentAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  "TransportEquipmentAnswersHelper" - {

    "addAnyTransportEquipmentYesNo" - {
      "must return No when TransportEquipment has not been answered in ie15/ie13" - {
        s"when $AddTransportEquipmentYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.addAnyTransportEquipmentYesNo()

              result must not be defined
          }
        }
      }

      "must return Yes when TransportEquipment has been answered in ie15/ie13" - {
        s"when $AddTransportEquipmentYesNoPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val setUserAnswers = userAnswers.setValue(AddTransportEquipmentYesNoPage, true)
              val helper         = new TransportEquipmentAnswersHelper(setUserAnswers, departureId, mode, activeIndex)
              val result         = helper.addAnyTransportEquipmentYesNo().get

              result.key.value mustEqual "Do you want to add any transport equipment?"
              result.value.value mustEqual "Yes"

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add any transport equipment"
              action.id mustEqual "change-add-transport-equipment"
          }
        }
      }
    }

    "containerIdentificationNumberYesNo" - {
      "must return None" - {
        "when AddContainerIdentificationNumberYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.addContainerIdentificationNumberYesNo()
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddContainerIdentificationNumberYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddContainerIdentificationNumberYesNoPage(index), true)

              val helper = TransportEquipmentAnswersHelper(answers, departureId, mode, activeIndex)
              val result = helper.addContainerIdentificationNumberYesNo().get

              result.key.value mustEqual "Do you want to add a container identification number?"
              result.value.value mustEqual "Yes"

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                .onPageLoad(departureId, mode, activeIndex)
                .url
              action.visuallyHiddenText.get mustEqual "if you want to add an identification number"
              action.id mustEqual "change-add-transport-equipment-container-identification-number-yes-no"

          }
        }
      }
    }

    "containerIdentificationNumber" - {
      "must return None" - {
        "when ContainerIdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.containerIdentificationNumber()
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ContainerIdentificationNumberPage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, containerIdentificationNumber) =>
              val answers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)

              val helper = TransportEquipmentAnswersHelper(answers, departureId, mode, index)
              val result = helper.containerIdentificationNumber().get

              result.key.value mustEqual "Container identification number"
              result.value.value mustEqual containerIdentificationNumber

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.index.routes.ContainerIdentificationNumberController
                .onPageLoad(departureId, mode, index)
                .url
              action.visuallyHiddenText.get mustEqual "identification number"
              action.id mustEqual "change-transport-equipment-container-identification-number"

          }
        }
      }
    }

    "sealsYesNo" - {
      "must return None" - {
        "when AddSealYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.sealsYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddSealYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddSealYesNoPage(index), true)

              val helper = new TransportEquipmentAnswersHelper(answers, departureId, mode, activeIndex)
              val result = helper.sealsYesNo.get

              result.key.value mustEqual "Do you want to add a seal?"
              result.value.value mustEqual "Yes"

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, index).url
              action.visuallyHiddenText.get mustEqual "if you want to add a seal"
              action.id mustEqual "change-add-seals"

          }
        }
      }
    }

    "seal" - {
      "must return None" - {
        "when seal is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.seal(sealIndex)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when seal is defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, sealIdNumber) =>
              val userAnswers = emptyUserAnswers.setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)
              val helper      = TransportEquipmentAnswersHelper(userAnswers, departureId, mode, activeIndex)
              val result      = helper.seal(index).get

              result.key.value mustEqual "Seal 1"
              result.value.value mustEqual sealIdNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                .onPageLoad(departureId, mode, equipmentIndex, sealIndex)
                .url
              action.visuallyHiddenText.get mustEqual "seal 1"
              action.id mustEqual "change-seal-1"
          }
        }
      }
    }

    "addOrRemoveSeals" - {
      "must return None" - {
        "when seals array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.addOrRemoveSeals()
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when seals array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SealSection(equipmentIndex, Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportEquipmentAnswersHelper(answers, departureId, mode, activeIndex)
              val result  = helper.addOrRemoveSeals().get

              result.id mustEqual "add-or-remove-seals"
              result.text mustEqual "Add or remove seals"
              result.href mustEqual controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex).url
          }
        }
      }
    }

    "addOrRemoveEquipments" - {
      "must return None" - {
        "when equipments array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, index)
              val result = helper.addOrRemoveEquipments()
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when equipments array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(EquipmentSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportEquipmentAnswersHelper(answers, departureId, mode, index)
              val result  = helper.addOrRemoveEquipments().get

              result.id mustEqual "add-or-remove-transport-equipment"
              result.text mustEqual "Add or remove transport equipment"
              result.href mustEqual controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode).url
          }
        }
      }
    }

    "item" - {
      "must return None" - {
        "when item is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.item(itemIndex)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when item is defined" in {
          forAll(arbitrary[Mode], nonEmptyString, positiveIntsMinMax(1, 100)) {
            (mode, description, itemNumber) =>
              val userAnswers =
                emptyUserAnswers.setValue(ItemPage(equipmentIndex, sealIndex), Item(declarationGoodsItemNumber = itemNumber, description = description))
              val helper = TransportEquipmentAnswersHelper(userAnswers, departureId, mode, activeIndex)
              val result = helper.item(itemIndex).get

              result.key.value mustEqual "Item 1"
              result.value.value mustEqual description
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.equipment.routes.SelectItemsController
                .onPageLoad(departureId, mode, equipmentIndex, sealIndex)
                .url
              action.visuallyHiddenText.get mustEqual "item 1"
              action.id mustEqual "change-item-1"
          }
        }
      }
    }

    "addOrRemoveItems" - {
      "must return None" - {
        "when items array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransportEquipmentAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.addOrRemoveItems()
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when seals array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ItemSection(equipmentIndex, Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransportEquipmentAnswersHelper(answers, departureId, mode, activeIndex)
              val result  = helper.addOrRemoveItems().get

              result.id mustEqual "add-or-remove-items"
              result.text mustEqual "Add or remove items from this transport equipment"
              result.href mustEqual controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex).url
          }
        }
      }
    }

  }
}
