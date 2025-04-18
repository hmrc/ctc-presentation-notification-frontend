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

import controllers.transport.border.active.routes
import models.*
import pages.*
import pages.transport.border.*
import play.api.mvc.Call

import javax.inject.Inject

class BorderGroupNavigator(nextIndex: Index) extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherBorderMeansOfTransportYesNoPage => ua => addAnotherBorderNavigation(ua, departureId, mode)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddAnotherBorderMeansOfTransportYesNoPage => ua => addAnotherBorderNavigation(ua, departureId, mode)
  }

  private def addAnotherBorderNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.get(AddAnotherBorderMeansOfTransportYesNoPage) flatMap {
      case true                       => Some(routes.IdentificationController.onPageLoad(departureId, mode, nextIndex))
      case false if mode == CheckMode => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case false                      => containerIndicatorCapturedNavigation(ua, departureId, mode)
    }
}

object BorderGroupNavigator {

  class BorderGroupNavigatorProvider @Inject() {

    def apply(nextIndex: Index): BorderGroupNavigator = new BorderGroupNavigator(nextIndex)
  }
}
