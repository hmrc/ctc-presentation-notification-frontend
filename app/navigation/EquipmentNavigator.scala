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

import akka.util.OptionVal
import models._
import models.messages.AuthorisationType
import pages._
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.{AddAnotherTransportEquipmentPage, RemoveTransportEquipmentPage}
import pages.transport.equipment.index.AddContainerIdentificationNumberYesNoPage
import play.api.mvc.Call

class EquipmentNavigator extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherTransportEquipmentPage(equipmentIndex: Index)   => ua => addAnotherTransportEquipmentRoute(ua, equipmentIndex, departureId, mode)
    case AddContainerIdentificationNumberYesNoPage(equipmentIndex) => ua => addContainerIdentificationNumberYesNoRoute(ua, equipmentIndex, departureId, mode)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  private def addAnotherTransportEquipmentRoute(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddAnotherTransportEquipmentPage(equipmentIndex)) match {
      case Some(true) =>
        ua.get(ContainerIndicatorPage) match {
          case Some(true) =>
            Some(
              controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController.onPageLoad(departureId, mode, equipmentIndex.next)
            )

          case _ =>
            (ua.departureData.isSimplified, ua.departureData.hasAuthC523) match {

              case (true, true) =>
                Some(
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                )
              case _ => Some(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))

            }

          case Some(false) => ??? //todo- redirect to CYA when built
        }
    }

  private def addContainerIdentificationNumberYesNoRoute(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] = {

    val isAuth523Present: Boolean = ua.departureData.Authorisation.flatMap(_.find(_.`type` == AuthorisationType.Other("C523"))).isDefined

    ua.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)) match {
      case Some(true) =>
        Some(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))
      case Some(false) if ua.departureData.isSimplified && isAuth523Present =>
        Some(controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0)))
      case Some(false) =>
        Some(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))
      case _ =>
        Some(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

}
