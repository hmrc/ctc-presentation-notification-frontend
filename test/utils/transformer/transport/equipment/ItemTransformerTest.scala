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

package utils.transformer.transport.equipment

import base.SpecBase
import models.messages.{GoodsReference, TransportEquipment}
import models.reference.Item
import models.{Index, UserAnswers}
import pages.transport.equipment.{AddAnotherTransportEquipmentPage, ItemPage}

class ItemTransformerTest extends SpecBase {

  val transformer = new ItemTransformer()

  "ItemTransformer" - {
    "must return updated answers with ItemPage if Items exist for the equipment" in {
      val userAnswersWithEquipments = UserAnswers.setTransportEquipmentLens
        .set(
          Option(
            List(
              TransportEquipment("1", None, 0, None, Some(List(GoodsReference("1", 1458)))),
              TransportEquipment("1", None, 0, None, Some(List(GoodsReference("2", 1458)))),
              TransportEquipment("1", None, 0, None, Some(List(GoodsReference("3", 1458))))
            )
          )
        )(emptyUserAnswers)
        .set(AddAnotherTransportEquipmentPage(Index(0)), true)
        .get
        .set(AddAnotherTransportEquipmentPage(Index(1)), true)
        .get
        .set(AddAnotherTransportEquipmentPage(Index(2)), true)
        .get

      userAnswersWithEquipments.get(ItemPage(Index(0), Index(0))) mustBe None
      userAnswersWithEquipments.get(ItemPage(Index(1), Index(0))) mustBe None
      userAnswersWithEquipments.get(ItemPage(Index(2), Index(0))) mustBe None
      userAnswersWithEquipments.get(ItemPage(Index(3), Index(0))) mustBe None

      whenReady(transformer.transform(hc)(userAnswersWithEquipments)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ItemPage(Index(0), Index(0))) mustBe Some(Item(1458, "descOfGoods"))
          updatedUserAnswers.get(ItemPage(Index(1), Index(0))) mustBe Some(Item(1458, "descOfGoods"))
          updatedUserAnswers.get(ItemPage(Index(2), Index(0))) mustBe Some(Item(1458, "descOfGoods"))
          updatedUserAnswers.get(ItemPage(Index(3), Index(0))) mustBe None

      }
    }
  }
}
