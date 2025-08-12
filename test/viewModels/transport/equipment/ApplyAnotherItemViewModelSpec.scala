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

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.transport.equipment.routes
import generators.Generators
import models.reference.Item
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.ItemPage
import viewModels.ListItem
import viewModels.transport.equipment.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider

class ApplyAnotherItemViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  "must get list items" - {

    "when there is one item" in {
      forAll(arbitrary[Mode], arbitrary[Item]) {
        (mode, item) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustEqual 1
          result.title mustEqual "You have applied 1 item to transport equipment 1"
          result.heading mustEqual "You have applied 1 item to transport equipment 1"
          result.legend mustEqual "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustEqual "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustEqual Seq(
            ListItem(
              name = s"Item ${item.toString}",
              changeUrl = routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
              removeUrl = None
            )
          )
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[Mode], arbitrary[Item], arbitrary[Item]) {
        (mode, item1, item2) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, itemIndex), item1)
            .setValue(ItemPage(equipmentIndex, Index(1)), item2)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex, isNumberItemsZero = false)

          result.listItems.length mustEqual 2
          result.title mustEqual "You have applied 2 items to transport equipment 1"
          result.heading mustEqual "You have applied 2 items to transport equipment 1"
          result.legend mustEqual "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustEqual "You cannot apply any more items. To apply another, you need to remove one first."

          result.listItems mustEqual Seq(
            ListItem(
              name = s"Item ${item1.toString}",
              changeUrl = routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
              removeUrl = Some(routes.RemoveItemController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url)
            ),
            ListItem(
              name = s"Item ${item2.toString}",
              changeUrl = routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url,
              removeUrl = Some(routes.RemoveItemController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url)
            )
          )
      }
    }
  }
}
