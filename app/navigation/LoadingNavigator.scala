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
import navigation.LocationOfGoodsNavigator.limitDatePageNavigator
import pages.Page
import pages.loading._
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
    ua.get(AddExtraInformationYesNoPage) match {
      case None => AddExtraInformationYesNoPage.route(ua, departureId, CheckMode)
      case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
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
    ua.get(AddExtraInformationYesNoPage) flatMap {
      case true  => CountryPage.route(ua, departureId, NormalMode)
      case false => locationPageNavigation(departureId, NormalMode, ua)
    }

  private def addExtraInformationYesNoCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    ua.get(AddExtraInformationYesNoPage) match {
      case Some(true) =>
        ua.get(CountryPage) match {
          case None => CountryPage.route(ua, departureId, CheckMode)
          case _    => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
}

object LoadingNavigator {

  private[navigation] def locationPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.isSimplified) {
      if (isLimitDateMissing(ua, mode)) {
        LimitDatePage.route(ua, departureId, mode)
      } else {
        limitDatePageNavigator(departureId, mode, ua)
      }
    } else {
      limitDatePageNavigator(departureId, mode, ua)
    }

  def isContainerIndicatorMissing(ua: UserAnswers, mode: Mode): Boolean =
    mode match {
      case NormalMode => ua.departureData.Consignment.containerIndicator.isEmpty
      case CheckMode  => ua.get(ContainerIndicatorPage).isEmpty
    }

  private def isLimitDateMissing(ua: UserAnswers, mode: Mode) =
    mode match {
      case NormalMode => ua.departureData.TransitOperation.limitDate.isEmpty
      case CheckMode  => ua.get(LimitDatePage).isEmpty
    }
}
