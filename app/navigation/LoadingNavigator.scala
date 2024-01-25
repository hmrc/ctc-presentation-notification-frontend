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
import models._
import navigation.LoadingNavigator._
import navigation.BorderNavigator._
import pages.Page
import pages.loading._
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.mvc.Call

@Singleton
class LoadingNavigator extends Navigator {

  protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddUnLocodeYesNoPage         => ua => addUnlocodeNormalRoute(ua, departureId)
    case UnLocodePage                 => ua => AddExtraInformationYesNoPage.route(ua, departureId, NormalMode)
    case AddExtraInformationYesNoPage => ua => addExtraInformationYesNoNormalRoute(ua, departureId)
    case CountryPage                  => ua => LocationPage.route(ua, departureId, NormalMode)
    case LocationPage                 => ua => locationPageNavigation(departureId, mode, ua)
  }

  protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddUnLocodeYesNoPage         => ua => addUnlocodeCheckRoute(ua, departureId)
    case UnLocodePage                 => ua => unLocodeCheckRoute(ua, departureId)
    case AddExtraInformationYesNoPage => ua => addExtraInformationYesNoCheckRoute(ua, departureId)
    case CountryPage                  => ua => LocationPage.route(ua, departureId, CheckMode)
    case LocationPage                 => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
  }

  private def unLocodeCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    (ua.get(AddExtraInformationYesNoPage), ua.departureData.Consignment.PlaceOfLoading.map(_.isAdditionalInformationPresent)) match {
      case (None, None) =>
        AddExtraInformationYesNoPage.route(ua, departureId, CheckMode)
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def addUnlocodeNormalRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddUnLocodeYesNoPage) match {
      case Some(true) =>
        UnLocodePage.route(ua, departureId, NormalMode)
      case _ => CountryPage.route(ua, departureId, NormalMode)
    }

  private def addUnlocodeCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddUnLocodeYesNoPage) match {
      case Some(true) =>
        ua.get(UnLocodePage) match {
          case None    => UnLocodePage.route(ua, departureId, CheckMode)
          case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => CountryPage.route(ua, departureId, CheckMode)
    }

  private def addExtraInformationYesNoNormalRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddExtraInformationYesNoPage) match {
      case Some(true) =>
        CountryPage.route(ua, departureId, NormalMode)
      case Some(false) => locationPageNavigation(departureId, NormalMode, ua)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def addExtraInformationYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddExtraInformationYesNoPage) match {
      case Some(true) =>
        (ua.get(CountryPage), ua.departureData.Consignment.PlaceOfLoading.flatMap(_.country)) match {
          case (None, None) => CountryPage.route(ua, departureId, CheckMode)
          case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
}

object LoadingNavigator {

  private[navigation] def locationPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.isSimplified) {
      ua.get(LimitDatePage) match {
        case Some(_) =>
          ua.get(ContainerIndicatorPage) match {
            case Some(_) => containerIndicatorPageNavigation(departureId, mode, ua)
            case None    => ContainerIndicatorPage.route(ua, departureId, mode)
          }
        case None => LimitDatePage.route(ua, departureId, mode)
      }
    } else ua.get(ContainerIndicatorPage) match {
      case Some(_) => containerIndicatorPageNavigation(departureId, mode, ua)
      case None => ContainerIndicatorPage.route(ua, departureId, mode)
    }

  private[navigation] def containerIndicatorPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.TransitOperation.isSecurityTypeInSet)
      BorderModeOfTransportPage.route(ua, departureId, mode)
    else borderModeOfTransportPageNavigation(ua, departureId, mode)

}
