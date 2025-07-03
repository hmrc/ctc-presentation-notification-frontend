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
import generated.*
import generators.Generators
import models.reference.Item
import models.{Index, SelectableList}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.ItemPage
import pages.transport.equipment.index.seals.SealIdentificationNumberPage

class SelectItemsViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "SelectItemsViewModel" - {

    "apply" - {

      "must filter list if there are items in two different transport equipments in UA" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 100,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item1",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item2",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              )
            )
          ),
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 200,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 3,
                declarationGoodsItemNumber = 3,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item3",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 4,
                declarationGoodsItemNumber = 4,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item4",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              )
            )
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .setValue(ItemPage(Index(0), Index(0)), Item(1, "item1"))
          .setValue(ItemPage(Index(0), Index(1)), Item(2, "item2"))
          .setValue(ItemPage(Index(1), Index(0)), Item(3, "item3"))
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers)

        val expectedResult = SelectItemsViewModel(SelectableList(Seq(Item(4, "item4"))))

        result mustBe expectedResult
      }

      "must filter list if there are items in two different transport equipments in UA without filtering selected item" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 100,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item1",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item2",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              )
            )
          ),
          HouseConsignmentType13(
            sequenceNumber = 2,
            grossMass = 200,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 3,
                declarationGoodsItemNumber = 3,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item3",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 4,
                declarationGoodsItemNumber = 4,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item4",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              )
            )
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .setValue(ItemPage(Index(0), Index(0)), Item(1, "item1"))
          .setValue(ItemPage(Index(0), Index(1)), Item(2, "item2"))
          .setValue(ItemPage(Index(1), Index(0)), Item(3, "item3"))
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers, Some(Item(1, "item1")))

        val expectedResult = SelectItemsViewModel(SelectableList(Seq(Item(4, "item4"), Item(1, "item1"))))

        result mustBe expectedResult
      }

      "must filter list if there are items in a single transport equipment in UA" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 100,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item1",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item2",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              )
            )
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .setValue(ItemPage(Index(0), Index(0)), Item(1, "item1"))
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers)

        val expectedResult = SelectItemsViewModel(SelectableList(Seq(Item(2, "item2"))))

        result mustBe expectedResult
      }

      "must return empty list if no items" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 0,
            ConsignmentItem = List.empty
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers)

        val expectedResult = new SelectItemsViewModel(SelectableList(Seq.empty))

        result mustBe expectedResult
      }

      "must not filter list if there is a transport equipment section but with no items" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 100,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item1",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item2",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              )
            )
          ),
          HouseConsignmentType13(
            sequenceNumber = 2,
            grossMass = 200,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 3,
                declarationGoodsItemNumber = 3,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item3",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 4,
                declarationGoodsItemNumber = 4,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item4",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 100
                  )
                )
              )
            )
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .setValue(SealIdentificationNumberPage(Index(0), Index(0)), "seal1")
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers)

        val expectedResult = SelectItemsViewModel(SelectableList(Seq(Item(1, "item1"), Item(2, "item2"), Item(3, "item3"), Item(4, "item4"))))

        result mustBe expectedResult
      }

      "must not filter list if there are no items in UA" in {

        val updatedHouseConsignment = Seq(
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 100,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item1",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item2",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 50
                  )
                )
              )
            )
          ),
          HouseConsignmentType13(
            sequenceNumber = 1,
            grossMass = 0,
            ConsignmentItem = Seq(
              ConsignmentItemType10(
                goodsItemNumber = 3,
                declarationGoodsItemNumber = 3,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item3",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 0
                  )
                )
              ),
              ConsignmentItemType10(
                goodsItemNumber = 4,
                declarationGoodsItemNumber = 4,
                Commodity = CommodityType10(
                  descriptionOfGoods = "item4",
                  GoodsMeasure = GoodsMeasureType04(
                    grossMass = 0
                  )
                )
              )
            )
          )
        )

        val updatedConsignment = emptyUserAnswers.departureData.Consignment.copy(HouseConsignment = updatedHouseConsignment)
        val departureData      = emptyUserAnswers.departureData.copy(Consignment = updatedConsignment)

        val userAnswers = emptyUserAnswers
          .copy(departureData = departureData)

        val result = SelectItemsViewModel.apply(userAnswers)

        val expectedResult = SelectItemsViewModel(SelectableList(Seq(Item(1, "item1"), Item(2, "item2"), Item(3, "item3"), Item(4, "item4"))))

        result mustBe expectedResult
      }
    }
  }
}
