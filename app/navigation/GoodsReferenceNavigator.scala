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

import com.google.inject.Singleton
import models.{Mode, UserAnswers}
import pages.Page
import pages.transport.equipment.ItemPage
import play.api.mvc.Call

@Singleton
class GoodsReferenceNavigator extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ItemPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
  }
}
