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

import generated.TransportEquipmentType03
import models.UserAnswers
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.{AddContainerIdentificationNumberYesNoPage, ContainerIdentificationNumberPage}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportEquipmentTransformer @Inject() (
  sealsTransformer: SealsTransformer,
  goodsReferencesTransformer: GoodsReferencesTransformer
)(implicit ec: ExecutionContext)
    extends NewPageTransformer {

  def transform(
    transportEquipments: Seq[TransportEquipmentType03]
  ): UserAnswers => Future[UserAnswers] =
    set(AddTransportEquipmentYesNoPage, transportEquipments.nonEmpty) andThen
      transportEquipments.mapWithSets {
        (value, equipmentIndex) =>
          set(AddContainerIdentificationNumberYesNoPage(equipmentIndex), value.containerIdentificationNumber.isDefined) andThen
            set(ContainerIdentificationNumberPage(equipmentIndex), value.containerIdentificationNumber) andThen
            sealsTransformer.transform(value.Seal, equipmentIndex) andThen
            goodsReferencesTransformer.transform(value.GoodsReference, equipmentIndex)
      }
}
