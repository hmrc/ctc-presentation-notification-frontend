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
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import play.api.mvc.Call

import javax.inject.Inject

class SealGroupNavigator(nextIndex: Index) extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherSealPage(equipmentIndex) => ua => addAnotherSealRoute(ua, departureId, mode, equipmentIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherSealPage(equipmentIndex) => ua => addAnotherSealRoute(ua, departureId, mode, equipmentIndex)
  }

  private def addAnotherSealRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(AddAnotherSealPage(equipmentIndex)) flatMap {
      case true                  => SealIdentificationNumberPage(equipmentIndex, nextIndex).route(ua, departureId, mode)
      case false if mode.isCheck => addAnotherSealCheckRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index)
      case false                 => ItemPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    }

  private def addAnotherSealCheckRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(ItemPage(equipmentIndex, Index(0))) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None    => ItemPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    }
}

object SealGroupNavigator {

  class SealGroupNavigatorProvider @Inject() {

    def apply(nextIndex: Index): SealGroupNavigator = new SealGroupNavigator(nextIndex)
  }
}
