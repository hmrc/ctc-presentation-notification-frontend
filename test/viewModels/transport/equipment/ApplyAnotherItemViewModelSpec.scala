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
import generators.Generators
import models.reference.Item
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.ItemPage
import viewModels.transport.equipment.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider

class ApplyAnotherItemViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one item" in {
      forAll(arbitrary[Mode], arbitrary[Item]) {
        (mode, item) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, sealIndex), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

          result.listItems.length mustBe 1
          result.title mustBe "You have applied 1 item to transport equipment 1"
          result.heading mustBe "You have applied 1 item to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."
      }
    }

    "when there are multiple items" in {

      forAll(arbitrary[Mode], arbitrary[Item]) {
        (mode, item) =>
          val userAnswers = emptyUserAnswers
            .setValue(ItemPage(equipmentIndex, sealIndex), item)
            .setValue(ItemPage(equipmentIndex, Index(1)), item)

          val result = new ApplyAnotherItemViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

          result.listItems.length mustBe 2
          result.title mustBe "You have applied 2 items to transport equipment 1"
          result.heading mustBe "You have applied 2 items to transport equipment 1"
          result.legend mustBe "Do any other items apply to transport equipment 1?"
          result.maxLimitLabel mustBe "You cannot apply any more items. To apply another, you need to remove one first."
      }
    }
  }
}
