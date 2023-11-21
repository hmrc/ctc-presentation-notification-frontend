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
import controllers.transport.border.active.routes
import models._
import models.reference.BorderMode
import navigation.BorderNavigator.borderModeOfTransportPageNavigation
import pages._
import pages.sections.transport.border.BorderActiveListSection
import pages.sections.transport.equipment.{EquipmentSection, EquipmentsSection}
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.border.active._
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class BorderNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {

    case BorderModeOfTransportPage                    => ua => borderModeOfTransportPageNavigation(ua, departureId, mode)
    case IdentificationPage(activeIndex)              => ua => IdentificationNumberPage(activeIndex).route(ua, departureId, mode)
    case IdentificationNumberPage(activeIndex)        => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
    case NationalityPage(activeIndex)                 => ua => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, mode)
    case CustomsOfficeActiveBorderPage(activeIndex)   => ua => customsOfficeNavigation(ua, departureId, mode, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex) => ua => addConveyanceNavigation(ua, departureId, mode, activeIndex)
    case ConveyanceReferenceNumberPage(activeIndex)   => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode, activeIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  private def customsOfficeNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    (ua.get(BorderModeOfTransportPage), ua.departureData.TransitOperation.isSecurityTypeInSet) match {
      case (Some(BorderMode("4", _)), true) =>
        Some(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))
      case _ => Some(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))
    }

  private def addConveyanceNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
      case Some(true)  => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, mode)
      case Some(false) => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode, activeIndex)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def redirectToAddAnotherActiveBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    if (ua.departureData.CustomsOfficeOfTransitDeclared.isDefined) {
      Some(routes.AddAnotherBorderTransportController.onPageLoad(departureId, mode))
    } else {
      //TODO: Change this when page is added
      Some(controllers.routes.MoreInformationController.onPageLoad(departureId))
    }

}

object BorderNavigator {

  private[navigation] def borderModeOfTransportPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] = {

    val numberOfActiveBorderMeans: Int = userAnswers.get(BorderActiveListSection).map(_.value.length).getOrElse(0)

    if (userAnswers.departureData.Consignment.isConsignmentActiveBorderTransportMeansEmpty && userAnswers.departureData.TransitOperation.isSecurityTypeInSet)
      transport.border.active.IdentificationPage(Index(numberOfActiveBorderMeans)).route(userAnswers, departureId, mode)
    else containerIndicatorRouting(userAnswers.departureData.Consignment.containerIndicator, userAnswers, departureId, mode)
  }

  private def checkTransitOperationSecurity(ua: UserAnswers): Boolean =
    ua.departureData.TransitOperation.isSecurityTypeInSet

  private[navigation] def containerIndicatorRouting(indicator: Option[String], userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] = {

    val numberOfEquipmentSection: Int = userAnswers.get(EquipmentsSection).map(_.value.length).getOrElse(0)

    indicator match {
      case Some(value) if value == "1" =>
        ContainerIdentificationNumberPage(equipmentIndex = Index(numberOfEquipmentSection))
          .route(userAnswers, departureId, mode)
      case Some(_) => AddTransportEquipmentYesNoPage.route(userAnswers, departureId, mode)
      case None    => ??? //TODO CYA 3811
    }
  }
}
