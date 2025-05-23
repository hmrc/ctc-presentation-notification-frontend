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
import config.Constants.TransportModeCode.*
import controllers.transport.border.active.routes
import models.*
import models.reference.TransportMode.BorderMode
import pages.*
import pages.transport.border.*
import pages.transport.border.active.*
import pages.transport.{AddInlandModeOfTransportYesNoPage, InlandModePage}
import play.api.mvc.Call

@Singleton
class BorderNavigator extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case BorderModeOfTransportPage                    => ua => borderModeOfTransportPageNavigation(ua, departureId, mode)
    case IdentificationPage(activeIndex)              => ua => IdentificationNumberPage(activeIndex).route(ua, departureId, mode)
    case IdentificationNumberPage(activeIndex)        => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
    case NationalityPage(activeIndex)                 => ua => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, mode)
    case CustomsOfficeActiveBorderPage(activeIndex)   => ua => customsOfficeNavigation(ua, departureId, mode, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex) => ua => addConveyanceNavigation(ua, departureId, mode, activeIndex)
    case ConveyanceReferenceNumberPage(_)             => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddBorderModeOfTransportYesNoPage            => ua => addBorderModeOfTransportYesNoNavigation(ua, departureId)
    case BorderModeOfTransportPage                    => ua => borderModeOfTransportCheckRoute(ua, departureId, mode)
    case IdentificationPage(activeIndex)              => ua => identificationCheckRoute(ua, departureId, activeIndex)
    case IdentificationNumberPage(activeIndex)        => ua => identificationNumberCheckRoute(ua, departureId, activeIndex)
    case NationalityPage(activeIndex)                 => ua => nationalityCheckRoute(ua, departureId, activeIndex)
    case CustomsOfficeActiveBorderPage(activeIndex)   => ua => customsOfficeCheckRoute(ua, departureId, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex) => ua => addConveyanceNavigationCheckRoute(ua, departureId, activeIndex)
    case ConveyanceReferenceNumberPage(_)             => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    case AddBorderMeansOfTransportYesNoPage           => ua => addBorderMeansOfTransportYesNoCheckRoute(ua, departureId)
    case AddInlandModeOfTransportYesNoPage            => ua => addInlandModeYesNoCheckRoute(ua, departureId)
    case InlandModePage                               => ua => inlandModeCheckRoute(ua, departureId, mode)
  }

  private def addBorderMeansOfTransportYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddBorderMeansOfTransportYesNoPage) match {
      case Some(true) => Some(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, CheckMode, Index(0)))
      case _          => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def borderModeOfTransportCheckRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (ua.departureData.hasSecurity) {
      Some(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)))
    } else {
      Some(controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode))
    }

  private def inlandModeCheckRoute(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(InlandModePage).map(_.code) match {
      case Some(Mail) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case _ => Some(controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, mode, Index(0)))
    }

  private def addInlandModeYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddInlandModeOfTransportYesNoPage) match {
      case Some(true) =>
        ua.get(InlandModePage) match {
          case None => Some(controllers.transport.routes.InlandModeController.onPageLoad(departureId, CheckMode))
          case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def addBorderModeOfTransportYesNoNavigation(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddBorderModeOfTransportYesNoPage) match {
      case Some(true) => borderModeOfTransportAlreadyAnswered(ua, departureId)
      case _          => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def identificationCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] =
    ua.get(IdentificationNumberPage(activeIndex)) match {
      case None => IdentificationNumberPage(activeIndex).route(ua, departureId, CheckMode)
      case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def identificationNumberCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] =
    ua.get(NationalityPage(activeIndex)) match {
      case None => NationalityPage(activeIndex).route(ua, departureId, CheckMode)
      case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def nationalityCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] =
    ua.get(CustomsOfficeActiveBorderPage(activeIndex)) match {
      case None => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, CheckMode)
      case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def customsOfficeCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] =
    (ua.get(BorderModeOfTransportPage), ua.departureData.hasSecurity) match {
      case (Some(BorderMode(Air, _)), true) =>
        ua.get(ConveyanceReferenceNumberPage(activeIndex)) match {
          case None => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, CheckMode)
          case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ =>
        ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
          case None => AddConveyanceReferenceYesNoPage(activeIndex).route(ua, departureId, CheckMode)
          case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
    }

  private def borderModeOfTransportAlreadyAnswered(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(BorderModeOfTransportPage) match {
      case None => BorderModeOfTransportPage.route(ua, departureId, CheckMode)
      case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def customsOfficeNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    (ua.get(BorderModeOfTransportPage), ua.departureData.hasSecurity) match {
      case (Some(BorderMode(Air, _)), true) =>
        Some(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))
      case _ => Some(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))
    }

  private def addConveyanceNavigationCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] =
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
      case Some(true) =>
        ua.get(ConveyanceReferenceNumberPage(activeIndex)) match {
          case None => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, CheckMode)
          case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def addConveyanceNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) flatMap {
      case true  => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, mode)
      case false => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
    }

  private def redirectToAddAnotherActiveBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (ua.departureData.CustomsOfficeOfTransitDeclared.nonEmpty) {
      Some(routes.AddAnotherBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode))
    } else {
      containerIndicatorCapturedNavigation(ua, departureId, mode)
    }
}
