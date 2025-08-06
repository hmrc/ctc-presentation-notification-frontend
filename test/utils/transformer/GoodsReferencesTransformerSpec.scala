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

package utils.transformer

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.*
import generators.Generators
import models.Index
import models.reference.Item
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.ItemPage

class GoodsReferencesTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[GoodsReferencesTransformer]

  "must transform data" in {
    forAll(
      arbitrary[TransportEquipmentType03],
      arbitrary[GoodsReferenceType01],
      arbitrary[HouseConsignmentType13],
      arbitrary[ConsignmentItemType10],
      arbitrary[CommodityType10]
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

        val goodsReferences = Seq(goodsReference.copy(declarationGoodsItemNumber = 1))

        val userAnswers = setTransportEquipmentLens.replace(
          Seq(transportEquipment.copy(GoodsReference = goodsReferences))
        )(emptyUserAnswers.copy(departureData = ie015))

        val result = transformer.transform(goodsReferences, Index(0)).apply(userAnswers).futureValue
        result.get(ItemPage(Index(0), Index(0))).value mustEqual Item(1, "Description")
    }
  }
}
