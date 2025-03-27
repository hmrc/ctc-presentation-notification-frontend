/*
 * Copyright 2025 HM Revenue & Customs
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
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.AddAnotherTransportEquipmentPage
import play.api.mvc.Call

import javax.inject.Inject

class EquipmentsNavigator(equipmentNavigator: EquipmentNavigator, nextIndex: Index) extends Navigator {

  override protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] =
    case AddAnotherTransportEquipmentPage => ua => addAnotherTransportEquipmentRoute(ua, departureId, mode)

  override protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] =
    case AddAnotherTransportEquipmentPage => ua => addAnotherTransportEquipmentRoute(ua, departureId, mode)

  private def addAnotherTransportEquipmentRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddAnotherTransportEquipmentPage) map {
      case true =>
        ua.get(ContainerIndicatorPage) match {
          case Some(true) =>
            controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController.onPageLoad(departureId, mode, nextIndex)
          case _ =>
            equipmentNavigator.checkProcedureAuthRoute(ua, departureId, mode, nextIndex)
        }
      case false =>
        controllers.routes.CheckYourAnswersController.onPageLoad(departureId)
    }
}

object EquipmentsNavigator {

  class EquipmentsNavigatorProvider @Inject() (equipmentNavigator: EquipmentNavigator) {

    def apply(nextIndex: Index): EquipmentsNavigator = new EquipmentsNavigator(equipmentNavigator, nextIndex)
  }
}
