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

import generated.TransportEquipmentType03
import models.reference.Item
import models.{Index, RichCC015CType, UserAnswers}
import pages.transport.equipment.ItemPage
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import scala.concurrent.Future

class ItemTransformer extends PageTransformer {

  override type DomainModelType              = Item
  override type ExtractedTypeInDepartureData = TransportEquipmentType03

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDeparture(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.TransportEquipment,
      generateCapturedAnswers = transportEquipments =>
        transportEquipments.zipWithIndex
          .flatMap {
            case (transportEquipment, equipmentIndex) =>
              transportEquipment.GoodsReference.zipWithIndex.flatMap {
                case (goodsReference, itemIndex) =>
                  userAnswers.departureData.items
                    .find(_.declarationGoodsItemNumber == goodsReference.declarationGoodsItemNumber)
                    .map(
                      item => (ItemPage(Index(equipmentIndex), Index(itemIndex)), item)
                    )
              }
          }
    )
}
