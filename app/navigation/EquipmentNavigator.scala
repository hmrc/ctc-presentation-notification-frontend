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
import models.{Index, Mode, NormalMode, RichCC015CType, UserAnswers}
import pages.Page
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.index._
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.{AddAnotherTransportEquipmentPage, AddTransportEquipmentYesNoPage, ItemPage}
import play.api.mvc.Call

@Singleton
class EquipmentNavigator extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddContainerIdentificationNumberYesNoPage(equipmentIndex) => ua => addContainerIdentificationNumberYesNoRoute(ua, equipmentIndex, departureId, mode)
    case ContainerIdentificationNumberPage(equipmentIndex)         => ua => checkProcedureAuthRoute(ua, departureId, mode, equipmentIndex)
    case AddTransportEquipmentYesNoPage                            => ua => addTransportEquipmentYesNoNormalRoute(ua, departureId, mode)
    case AddSealYesNoPage(equipmentIndex)                          => ua => addSealYesNoNormalRoute(ua, departureId, mode, equipmentIndex)
    case SealIdentificationNumberPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex))
    case AddAnotherSealPage(equipmentIndex, sealIndex)           => ua => addAnotherSealRoute(ua, departureId, mode, equipmentIndex, sealIndex)
    case AddAnotherTransportEquipmentPage(equipmentIndex: Index) => ua => addAnotherTransportEquipmentRoute(ua, equipmentIndex, departureId, mode)
    case ItemPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
    case ApplyAnotherItemPage(equipmentIndex, itemIndex) => ua => applyAnotherItemRoute(ua, departureId, mode, equipmentIndex, itemIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddContainerIdentificationNumberYesNoPage(equipmentIndex) =>
      ua => addContainerIdentificationNumberYesNoCheckRoute(ua, equipmentIndex, departureId, NormalMode)
    case ContainerIdentificationNumberPage(equipmentIndex) => ua => containerIdentificationNumberRoute(ua, departureId, equipmentIndex)
    case AddTransportEquipmentYesNoPage                    => ua => addTransportEquipmentYesNoNormalRoute(ua, departureId, mode)

    case AddSealYesNoPage(equipmentIndex) => ua => addSealYesNoNormalRoute(ua, departureId, mode, equipmentIndex)
    case SealIdentificationNumberPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex))
    case AddAnotherSealPage(equipmentIndex, sealIndex) => ua => addAnotherSealRoute(ua, departureId, mode, equipmentIndex, sealIndex)
    case ItemPage(equipmentIndex, _) =>
      _ => Some(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
    case ApplyAnotherItemPage(equipmentIndex, itemIndex)         => ua => applyAnotherItemRoute(ua, departureId, mode, equipmentIndex, itemIndex)
    case AddAnotherTransportEquipmentPage(equipmentIndex: Index) => ua => addAnotherTransportEquipmentRoute(ua, equipmentIndex, departureId, mode)
  }

  private def containerIdentificationNumberRoute(ua: UserAnswers, departureId: String, equipmentIndex: Index): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None    => Some(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, NormalMode, equipmentIndex))
    }

  private def applyAnotherItemRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index, itemIndex: Index): Option[Call] =
    ua.get(ApplyAnotherItemPage(equipmentIndex, itemIndex)) flatMap {
      case true                  => ItemPage(equipmentIndex, itemIndex).route(ua, departureId, mode)
      case false if mode.isCheck => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case false                 => Some(controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode))
    }

  private def addContainerIdentificationNumberYesNoRoute(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)) map {
      case true =>
        controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex)
      case false if ua.departureData.isSimplified && ua.departureData.hasAuthC523 =>
        controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0))
      case false =>
        controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
    }

  private def addContainerIdentificationNumberYesNoCheckRoute(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)) match {
      case Some(true) =>
        Some(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))

      case Some(false) => checkSealPopulated(ua, equipmentIndex, departureId, mode)
      case _           => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def checkSealPopulated(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None    => Some(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))
    }

  private def addSealYesNoNormalRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(AddSealYesNoPage(equipmentIndex)) flatMap {
      case true                  => SealIdentificationNumberPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
      case false if mode.isCheck => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case false                 => ItemPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    }

  private def checkProcedureAuthRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    if (ua.departureData.isSimplified && ua.departureData.hasAuthC523) {
      SealIdentificationNumberPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    } else {
      AddSealYesNoPage(equipmentIndex).route(ua, departureId, mode)
    }

  def addAnotherSealRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index, sealIndex: Index): Option[Call] =
    ua.get(AddAnotherSealPage(equipmentIndex, sealIndex)) flatMap {
      case true                  => SealIdentificationNumberPage(equipmentIndex, sealIndex).route(ua, departureId, mode)
      case false if mode.isCheck => addAnotherSealCheckRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index)
      case false                 => ItemPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    }

  def addAnotherSealCheckRoute(ua: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): Option[Call] =
    ua.get(ItemPage(equipmentIndex, Index(0))) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None    => ItemPage(equipmentIndex, Index(0)).route(ua, departureId, mode)
    }

  private def addTransportEquipmentYesNoNormalRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddTransportEquipmentYesNoPage) match {
      case Some(true) => checkProcedureAuthRoute(ua, departureId, mode, Index(0))
      case _          => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def addAnotherTransportEquipmentRoute(ua: UserAnswers, equipmentIndex: Index, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddAnotherTransportEquipmentPage(equipmentIndex)) map {
      case true =>
        ua.get(ContainerIndicatorPage) match {
          case Some(true) =>
            controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController.onPageLoad(departureId, mode, equipmentIndex)
          case _ if ua.departureData.isSimplified && ua.departureData.hasAuthC523 =>
            controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0))
          case _ =>
            controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
        }
      case false =>
        controllers.routes.CheckYourAnswersController.onPageLoad(departureId)
    }
}
