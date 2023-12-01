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
import config.Constants.Air
import controllers.transport.border.active.routes
import models._
import models.reference.BorderMode
import navigation.BorderNavigator.{borderModeOfTransportPageNavigation, containerIndicatorRouting}
import pages._
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.ContainerIndicatorPage
import pages.transport.border.active._
import pages.transport.border.{AddAnotherBorderModeOfTransportPage, BorderModeOfTransportPage}
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class BorderNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {

    case BorderModeOfTransportPage                        => ua => borderModeOfTransportPageNavigation(ua, departureId, mode)
    case IdentificationPage(activeIndex)                  => ua => IdentificationNumberPage(activeIndex).route(ua, departureId, mode)
    case IdentificationNumberPage(activeIndex)            => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
    case NationalityPage(activeIndex)                     => ua => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, mode)
    case CustomsOfficeActiveBorderPage(activeIndex)       => ua => customsOfficeNavigation(ua, departureId, mode, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex)     => ua => addConveyanceNavigation(ua, departureId, mode, activeIndex)
    case ConveyanceReferenceNumberPage(activeIndex)       => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
    case AddAnotherBorderModeOfTransportPage(activeIndex) => ua => addAnotherBorderNavigation(ua, departureId, mode, activeIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  private def customsOfficeNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    (ua.get(BorderModeOfTransportPage), ua.departureData.TransitOperation.isSecurityTypeInSet) match {
      case (Some(BorderMode(Air, _)), true) =>
        Some(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))
      case _ => Some(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))
    }

  private def addConveyanceNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
      case Some(true)  => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, mode)
      case Some(false) => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def addAnotherBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddAnotherBorderModeOfTransportPage(activeIndex)) match {
      case Some(true)  => Some(routes.IdentificationController.onPageLoad(departureId, mode, activeIndex))
      case Some(false) => containerIndicatorRouting(ua, departureId, mode)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def redirectToAddAnotherActiveBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (ua.departureData.CustomsOfficeOfTransitDeclared.isDefined) {
      Some(routes.AddAnotherBorderTransportController.onPageLoad(departureId, mode))
    } else {
      containerIndicatorRouting(ua, departureId, mode)
    }
}

object BorderNavigator {

  private[navigation] def borderModeOfTransportPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] = {

    val numberOfActiveBorderMeans: Int = userAnswers.get(BorderActiveListSection).map(_.value.length - 1).getOrElse(0)

    if (userAnswers.departureData.Consignment.isConsignmentActiveBorderTransportMeansEmpty && userAnswers.departureData.TransitOperation.isSecurityTypeInSet)
      transport.border.active.IdentificationPage(Index(numberOfActiveBorderMeans)).route(userAnswers, departureId, mode)
    else containerIndicatorRouting(userAnswers, departureId, mode)
  }

  private[navigation] def containerIndicatorRouting(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(ContainerIndicatorPage) match {
      case Some(true) =>
        ContainerIdentificationNumberPage(Index(0))
          .route(userAnswers, departureId, mode)
      case Some(false) => AddTransportEquipmentYesNoPage.route(userAnswers, departureId, mode)
      case None        => Some(controllers.routes.SessionExpiredController.onPageLoad()) //TODO CYA 3811
    }
}
