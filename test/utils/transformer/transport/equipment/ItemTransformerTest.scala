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
import generated._
import generators.Generators
import models.Index
import models.reference.Item
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.ItemPage

class ItemTransformerTest extends SpecBase with Generators {

  val transformer = new ItemTransformer()

  "ItemTransformer" - {
    "must return updated answers with ItemPage if Items exist for the equipment" in {
      forAll(
        arbitrary[TransportEquipmentType06],
        arbitrary[GoodsReferenceType02],
        arbitrary[HouseConsignmentType10],
        arbitrary[ConsignmentItemType09],
        arbitrary[CommodityType07]
      ) {
        (transportEquipment, goodsReference, houseConsignment, consignmentItem, commodity) =>
          val ie015 = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(
              HouseConsignment = Seq(
                houseConsignment.copy(
                  ConsignmentItem = Seq(
                    consignmentItem.copy(
                      declarationGoodsItemNumber = 1,
                      Commodity = commodity.copy(
                        descriptionOfGoods = "Description"
                      )
                    )
                  )
                )
              )
            )
          )

          val userAnswers = setTransportEquipmentLens.set(
            Seq(transportEquipment.copy(GoodsReference = Seq(goodsReference.copy(declarationGoodsItemNumber = 1))))
          )(emptyUserAnswers.copy(departureData = ie015))

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(ItemPage(Index(0), Index(0))).value mustBe Item(1, "Description")
      }
    }
  }
}
