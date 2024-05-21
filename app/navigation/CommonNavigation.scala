/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{CheckMode, Index, Mode, NormalMode, RichCC015CType, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import pages.transport
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.mvc.Call

trait CommonNavigation {

  protected def borderModeOfTransportPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isEmpty && userAnswers.departureData.hasSecurity) {
      val numberOfActiveBorderMeans: Int = userAnswers.get(BorderActiveListSection).map(_.value.length - 1).getOrElse(0)
      transport.border.active.IdentificationPage(Index(numberOfActiveBorderMeans)).route(userAnswers, departureId, mode)
    } else {
      containerIndicatorCapturedNavigation(userAnswers, departureId, mode)
    }

  protected def containerIndicatorCapturedNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    if (userAnswers.departureData.Consignment.containerIndicator.isDefined) {
      Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    } else {
      userAnswers.get(ContainerIndicatorPage) match {
        case Some(true)  => ContainerIdentificationNumberPage(Index(0)).route(userAnswers, departureId, mode)
        case Some(false) => AddTransportEquipmentYesNoPage.route(userAnswers, departureId, mode)
        case None        => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }
    }

  protected def containerIndicatorPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.hasSecurity)
      BorderModeOfTransportPage.route(ua, departureId, mode)
    else
      borderModeOfTransportPageNavigation(ua, departureId, mode)

  protected def locationPageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (ua.departureData.isSimplified && isLimitDateMissing(ua, mode)) {
      LimitDatePage.route(ua, departureId, mode)
    } else {
      limitDatePageNavigation(departureId, mode, ua)
    }

  protected def limitDatePageNavigation(departureId: String, mode: Mode, ua: UserAnswers): Option[Call] =
    if (isContainerIndicatorMissing(ua, mode)) {
      ContainerIndicatorPage.route(ua, departureId, mode)
    } else {
      containerIndicatorPageNavigation(departureId, mode, ua)
    }

  private def isContainerIndicatorMissing(ua: UserAnswers, mode: Mode): Boolean =
    mode match {
      case NormalMode => ua.departureData.Consignment.containerIndicator.isEmpty
      case CheckMode  => ua.get(ContainerIndicatorPage).isEmpty
    }

  private def isLimitDateMissing(ua: UserAnswers, mode: Mode): Boolean =
    mode match {
      case NormalMode => ua.departureData.TransitOperation.limitDate.isEmpty
      case CheckMode  => ua.get(LimitDatePage).isEmpty
    }
}
