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
import pages.Page
import pages.loading._
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.mvc.Call

@Singleton
class LoadingNavigator {

  protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddUnLocodeYesNoPage         => ua => addUnlocodeNormalRoute(ua, departureId)
    case UnLocodePage                 => ua => AddExtraInformationYesNoPage.route(ua, departureId, NormalMode)
    case AddExtraInformationYesNoPage => ua => addExtraInformationYesNoNormalRoute(ua, departureId)
    case CountryPage                  => ua => LocationPage.route(ua, departureId, NormalMode)
    case LocationPage                 => ua => locationPageNavigation(departureId, mode, ua)
    case LimitDatePage                => ua => limitDatePageNavigator(departureId, mode, ua)
  }

  private def limitDatePageNavigator(departureId: String, mode: Mode, ua: UserAnswers) =
    ua.departureData.Consignment.containerIndicator match {
      case Some(_) =>
        borderPageNavigation(departureId, mode, ua)
      case None => ContainerIndicatorPage.route(ua, departureId, mode)
    }

  private def locationPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.isSimplified) {
      ua.departureData.TransitOperation.limitDate match {
        case Some(_) =>
          if (ua.departureData.Consignment.containerIndicator.isEmpty) {
            ContainerIndicatorPage.route(ua, departureId, mode)
          } else borderPageNavigation(departureId, mode, ua)
        case None => LimitDatePage.route(ua, departureId, mode)
      }
    } else if (ua.departureData.Consignment.containerIndicator.isEmpty | ua.departureData.TransitOperation.limitDate.isEmpty) {
      ContainerIndicatorPage.route(ua, departureId, mode)
    } else ???

  private def borderPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] = if (ua.departureData.TransitOperation.isSecurityTypeInSet)
    BorderModeOfTransportPage.route(ua, departureId, mode)
  else ??? //TODO follow false path of transitOperationSecurity (<CC015C-TRANSIT OPERATION.Security> is in SET {1,2,3})

  protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = { //todo add when CYA page built
    case AddUnLocodeYesNoPage         => ua => addUnlocodeCheckRoute(ua, departureId)
    case UnLocodePage                 => ua => ??? //todo will go back to CYA
    case AddExtraInformationYesNoPage => ua => addExtraInformationYesNoCheckRoute(ua, departureId)
    case CountryPage                  => ua => ??? //todo will go back to CYA
  }

  def addUnlocodeNormalRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddUnLocodeYesNoPage) match {
      case Some(true) =>
        UnLocodePage.route(ua, departureId, NormalMode)
      case _ => CountryPage.route(ua, departureId, NormalMode)
    }

  def addUnlocodeCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddUnLocodeYesNoPage) match {
      case Some(true) =>
        ua.get(UnLocodePage) match {
          case Some(_) => ??? //todo go to cya
          case _       => UnLocodePage.route(ua, departureId, CheckMode)
        }

      case _ => ??? // todo will go to CYA page
    }

  def addExtraInformationYesNoNormalRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddExtraInformationYesNoPage) match {
      case Some(true) =>
        CountryPage.route(ua, departureId, NormalMode)
      case _ => LocationPage.route(ua, departureId, NormalMode)
    }

  def addExtraInformationYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddExtraInformationYesNoPage) match {
      case Some(true) =>
        ua.get(CountryPage) match {
          case Some(_) => ??? //todo  will go to cya
          case _       => CountryPage.route(ua, departureId, CheckMode)
        }
      case _ => ??? // todo will go to CYA page
    }

  private def handleCall(userAnswers: UserAnswers, call: UserAnswers => Option[Call]) =
    call(userAnswers) match {
      case Some(onwardRoute) => onwardRoute
      case _                 => ??? //TODO add error page
    }

  def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call =
    mode match {
      case NormalMode =>
        normalRoutes(departureId, mode).lift(page) match {
          case None       => controllers.routes.IndexController.index(departureId)
          case Some(call) => handleCall(userAnswers, call)
        }
      case CheckMode =>
        checkRoutes(departureId, mode).lift(page) match {
          case None       => controllers.routes.IndexController.index(departureId)
          case Some(call) => handleCall(userAnswers, call)
        }
    }

}
