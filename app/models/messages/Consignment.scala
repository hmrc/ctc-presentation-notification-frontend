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

package models.messages

import models.reference.Item
import play.api.libs.json._

case class Consignment(
  containerIndicator: Option[String],
  modeOfTransportAtTheBorder: Option[String],
  TransportEquipment: Option[Seq[TransportEquipment]],
  LocationOfGoods: Option[LocationOfGoods],
  ActiveBorderTransportMeans: Option[Seq[ActiveBorderTransportMeans]],
  PlaceOfLoading: Option[PlaceOfLoading],
  HouseConsignment: Seq[HouseConsignment]
) {
  def isConsignmentActiveBorderTransportMeansEmpty: Boolean = ActiveBorderTransportMeans.toList.flatten.isEmpty
  val isPlaceOfLoadingPresent: Boolean                      = PlaceOfLoading.isDefined

  val allItems: Seq[Item] =
    HouseConsignment
      .foldLeft(Seq.empty[Item]) {
        (listOfItems, houseConsignment) =>
          houseConsignment.ConsignmentItem.map(
            item => Item(item.declarationGoodsItemNumber, item.Commodity.descriptionOfGoods)
          ) ++ listOfItems
      }
      .sortBy(_.goodsItemNumber)
}

object Consignment {
  implicit val format: OFormat[Consignment] = Json.format[Consignment]
}
