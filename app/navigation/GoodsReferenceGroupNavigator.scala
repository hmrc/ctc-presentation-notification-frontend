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

package navigation

import models.{Index, Mode, UserAnswers}
import pages.Page
import pages.transport.equipment.ItemPage
import pages.transport.equipment.index.*
import play.api.mvc.Call

import javax.inject.Inject

class GoodsReferenceGroupNavigator(nextGoodsReferenceIndex: Index) extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ApplyAnotherItemPage(equipmentIndex) => ua => applyAnotherItemRoute(ua, departureId, mode, equipmentIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ApplyAnotherItemPage(equipmentIndex) => ua => applyAnotherItemRoute(ua, departureId, mode, equipmentIndex)
  }

  private def applyAnotherItemRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(ApplyAnotherItemPage(equipmentIndex)) flatMap {
      case true                  => ItemPage(equipmentIndex, nextGoodsReferenceIndex).route(ua, departureId, mode)
      case false if mode.isCheck => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case false                 => Some(controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode))
    }
}

object GoodsReferenceGroupNavigator {

  class GoodsReferenceGroupNavigatorProvider @Inject() {

    def apply(nextIndex: Index): GoodsReferenceGroupNavigator = new GoodsReferenceGroupNavigator(nextIndex)
  }
}
