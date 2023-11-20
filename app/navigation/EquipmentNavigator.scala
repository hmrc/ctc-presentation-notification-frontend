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
import models.messages.AuthorisationType
import models.{Index, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.{AddSealYesNoPage, ContainerIdentificationNumberPage}
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import play.api.mvc.Call

@Singleton
class EquipmentNavigator extends Navigator {

  protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ContainerIdentificationNumberPage(equipmentIndex) => ua => containerIdentificationNumberRoute(ua, departureId, NormalMode, equipmentIndex)
    case AddTransportEquipmentYesNoPage                    => ua => addTransportEquipmentYesNoNormalRoute(ua, departureId, NormalMode)
    case AddSealYesNoPage(equipmentIndex)                  => ua => addSealYesNoNormalRoute(ua, departureId, NormalMode, equipmentIndex)
    case SealIdentificationNumberPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex))
  }

  protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  def containerIdentificationNumberRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] = {

    val isAuth523Present: Boolean = ua.departureData.Authorisation.flatMap(_.find(_.`type` == AuthorisationType.Other("C523"))).isDefined

    if (ua.departureData.isSimplified && isAuth523Present) {
      SealIdentificationNumberPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    } else {
      AddSealYesNoPage(equipmentIndex).route(ua, departureId, mode)
    }
  }

  def addTransportEquipmentYesNoNormalRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddTransportEquipmentYesNoPage) match {
      case Some(true) => containerIdentificationNumberRoute(ua, departureId, mode, Index(0))
      case _          => ??? // TODO Should go CYA
    }

  def addSealYesNoNormalRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)) match {
      case Some(true) => SealIdentificationNumberPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
      case _          => ??? // TODO Should go goods reference item page
    }
}
