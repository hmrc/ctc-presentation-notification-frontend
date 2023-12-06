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
import pages._
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.border.active._
import pages.transport.border.{AddBorderMeansOfTransportYesNoPage, AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class BorderNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {

    case BorderModeOfTransportPage                    => ua => borderModeNavigation(ua, departureId, mode)
    case IdentificationPage(activeIndex)              => ua => IdentificationNumberPage(activeIndex).route(ua, departureId, mode)
    case IdentificationNumberPage(activeIndex)        => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
    case NationalityPage(activeIndex)                 => ua => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, mode)
    case CustomsOfficeActiveBorderPage(activeIndex)   => ua => customsOfficeNavigation(ua, departureId, mode, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex) => ua => addConveyanceNavigation(ua, departureId, mode, activeIndex)
    case ConveyanceReferenceNumberPage(activeIndex)   => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode, activeIndex)

  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddBorderModeOfTransportYesNoPage            => ua => addBorderModeOfTransportYesNoNavigation(ua, departureId)
    case BorderModeOfTransportPage                    => ua => borderModeOfTransportCheckRoute(ua, departureId, mode)
    case IdentificationPage(activeIndex)              => ua => IdentificationNumberPage(activeIndex).route(ua, departureId, mode)
    case IdentificationNumberPage(activeIndex)        => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
    case NationalityPage(activeIndex)                 => ua => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, mode)
    case CustomsOfficeActiveBorderPage(activeIndex)   => ua => customsOfficeNavigation(ua, departureId, mode, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex) => ua => addConveyanceNavigation(ua, departureId, mode, activeIndex)
    case ConveyanceReferenceNumberPage(activeIndex)   => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode, activeIndex)
    case AddBorderMeansOfTransportYesNoPage           => ua => addBorderMeansOfTransportYesNoRoute(ua, departureId, mode)
  }

  private def addBorderMeansOfTransportYesNoRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddBorderMeansOfTransportYesNoPage) match {
      case Some(true)  => Some(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)))
      case Some(false) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None        => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def borderModeOfTransportCheckRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.departureData.TransitOperation.isSecurityTypeInSet match {
      case true  => Some(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)))
      case false => Some(controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode))
    }

  private def addBorderModeOfTransportYesNoNavigation(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddBorderModeOfTransportYesNoPage) match {
      case Some(true) => borderModeOfTransportAlreadyAnswered(ua, departureId)
      case _          => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def borderModeOfTransportAlreadyAnswered(ua: UserAnswers, departureId: String) =
    (ua.get(BorderModeOfTransportPage), ua.departureData.Consignment.modeOfTransportAtTheBorder) match {
      case (None, None) => BorderModeOfTransportPage.route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def borderModeNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] = {
    val numberOfActiveBorderMeans: Int = ua.get(BorderActiveListSection).map(_.value.length).getOrElse(0)

    (ua.departureData.TransitOperation.isSecurityTypeInSet, ua.departureData.Consignment.ActiveBorderTransportMeans.isDefined) match {
      //TODO: Change route for _ case when page has been added
      case (true, false) =>
        Some(routes.IdentificationController.onPageLoad(departureId, mode, Index(numberOfActiveBorderMeans)))
      case _ => Some(controllers.routes.MoreInformationController.onPageLoad(departureId))
    }
  }

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

    if (userAnswers.departureData.Consignment.isConsignmentActiveBorderTransportMeansEmpty && checkTransitOperationSecurity(userAnswers))
      transport.border.active.IdentificationPage(Index(numberOfActiveBorderMeans)).route(userAnswers, departureId, mode)
    else ??? //TODO follow false path
  }

  private def checkTransitOperationSecurity(ua: UserAnswers): Boolean =
    ua.departureData.TransitOperation.isSecurityTypeInSet
}
