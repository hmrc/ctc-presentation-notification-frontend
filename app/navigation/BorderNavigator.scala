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
import models.reference.TransportMode.BorderMode
import navigation.BorderNavigator.{borderModeOfTransportPageNavigation, containerIndicatorRouting}
import pages._
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.ContainerIndicatorPage
import pages.transport.border.active._
import pages.transport.border.{
  AddAnotherBorderModeOfTransportPage,
  AddBorderMeansOfTransportYesNoPage,
  AddBorderModeOfTransportYesNoPage,
  BorderModeOfTransportPage
}
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
    case ConveyanceReferenceNumberPage(_)                 => ua => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
    case AddAnotherBorderModeOfTransportPage(activeIndex) => ua => addAnotherBorderNavigation(ua, departureId, mode, activeIndex)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddBorderModeOfTransportYesNoPage                => ua => addBorderModeOfTransportYesNoNavigation(ua, departureId)
    case BorderModeOfTransportPage                        => ua => borderModeOfTransportCheckRoute(ua, departureId, mode)
    case IdentificationPage(activeIndex)                  => ua => identificationCheckRoute(ua, departureId, activeIndex)
    case IdentificationNumberPage(activeIndex)            => ua => identificationNumberCheckRoute(ua, departureId, activeIndex)
    case NationalityPage(activeIndex)                     => ua => nationalityCheckRoute(ua, departureId, activeIndex)
    case CustomsOfficeActiveBorderPage(activeIndex)       => ua => customsOfficeCheckRoute(ua, departureId, activeIndex)
    case AddConveyanceReferenceYesNoPage(activeIndex)     => ua => addConveyanceNavigationCheckRoute(ua, departureId, activeIndex)
    case ConveyanceReferenceNumberPage(_)                 => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    case AddBorderMeansOfTransportYesNoPage               => ua => addBorderMeansOfTransportYesNoCheckRoute(ua, departureId)
    case AddAnotherBorderModeOfTransportPage(activeIndex) => ua => addAnotherBorderNavigation(ua, departureId, mode, activeIndex)
  }

  private def addBorderMeansOfTransportYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] = {
    val ie015ActiveBorderListSection = ua.departureData.Consignment.ActiveBorderTransportMeans
    ua.get(AddBorderMeansOfTransportYesNoPage) match {
      case Some(true) =>
        (ua.get(BorderActiveListSection), ie015ActiveBorderListSection) match {
          case (None, None) => Some(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, CheckMode, Index(0)))
          case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
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

  private def identificationCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] = {
    val ie015IdentificationNumber =
      ua.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.lift(activeIndex.position).flatMap(_.identificationNumber))
    (ua.get(IdentificationNumberPage(activeIndex)), ie015IdentificationNumber) match {
      case (None, None) => IdentificationNumberPage(activeIndex).route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

  private def identificationNumberCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] = {
    val ie015Nationality = ua.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.lift(activeIndex.position).flatMap(_.nationality))
    (ua.get(NationalityPage(activeIndex)), ie015Nationality) match {
      case (None, None) => NationalityPage(activeIndex).route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

  private def nationalityCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] = {
    val ie015CustomsOfficeAtBorder =
      ua.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.lift(activeIndex.position).flatMap(_.customsOfficeAtBorderReferenceNumber))
    (ua.get(CustomsOfficeActiveBorderPage(activeIndex)), ie015CustomsOfficeAtBorder) match {
      case (None, None) => CustomsOfficeActiveBorderPage(activeIndex).route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

  private def customsOfficeCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] = {
    val ie015ConveyanceRefNumber =
      ua.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.lift(activeIndex.position).map(_.conveyanceReferenceNumber.isDefined))

    (ua.get(BorderModeOfTransportPage), ua.departureData.TransitOperation.isSecurityTypeInSet) match {
      case (Some(BorderMode(Air, _)), true) =>
        (ua.get(ConveyanceReferenceNumberPage(activeIndex)), ie015ConveyanceRefNumber) match {
          case (None, None) => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, CheckMode)
          case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ =>
        (ua.get(AddConveyanceReferenceYesNoPage(activeIndex)), ie015ConveyanceRefNumber) match {
          case (None, None) => AddConveyanceReferenceYesNoPage(activeIndex).route(ua, departureId, CheckMode)
          case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
    }
  }

  private def borderModeOfTransportAlreadyAnswered(ua: UserAnswers, departureId: String) =
    (ua.get(BorderModeOfTransportPage), ua.departureData.Consignment.modeOfTransportAtTheBorder) match {
      case (None, None) => BorderModeOfTransportPage.route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def customsOfficeNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    (ua.get(BorderModeOfTransportPage), ua.departureData.TransitOperation.isSecurityTypeInSet) match {
      case (Some(BorderMode(Air, _)), true) =>
        Some(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))
      case _ => Some(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))
    }

  private def addConveyanceNavigationCheckRoute(ua: UserAnswers, departureId: String, activeIndex: Index): Option[Call] = {
    val ie015ConveyanceReferenceNumber =
      ua.departureData.Consignment.ActiveBorderTransportMeans.flatMap(_.lift(activeIndex.position).flatMap(_.conveyanceReferenceNumber))
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
      case Some(true) =>
        (ua.get(ConveyanceReferenceNumberPage(activeIndex)), ie015ConveyanceReferenceNumber) match {
          case (None, None) => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, CheckMode)
          case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

  private def addConveyanceNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddConveyanceReferenceYesNoPage(activeIndex)) match {
      case Some(true)  => ConveyanceReferenceNumberPage(activeIndex).route(ua, departureId, mode)
      case Some(false) => redirectToAddAnotherActiveBorderNavigation(ua, departureId, mode)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def addAnotherBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] =
    ua.get(AddAnotherBorderModeOfTransportPage(activeIndex)) match {
      case Some(true)                       => Some(routes.IdentificationController.onPageLoad(departureId, mode, activeIndex))
      case Some(false) if mode == CheckMode => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case Some(false)                      => containerIndicatorRouting(ua, departureId, mode)
      case _                                => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def redirectToAddAnotherActiveBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (ua.departureData.CustomsOfficeOfTransitDeclared.isDefined) {
      Some(routes.AddAnotherBorderTransportController.onPageLoad(departureId, mode))
    } else {
      ua.get(ContainerIndicatorPage) match {
        case Some(_) => containerIndicatorRouting(ua, departureId, mode)
        case None    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }

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
      case None        => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
}
