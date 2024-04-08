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

import controllers.routes
import logging.Logging
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

trait Navigator extends Logging {
  private type RouteMapping = PartialFunction[Page, UserAnswers => Option[Call]]

  protected def normalRoutes(departureId: String, mode: Mode): RouteMapping

  protected def checkRoutes(departureId: String, mode: Mode): RouteMapping

  def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = {
    def handleCall(call: UserAnswers => Option[Call]) =
      call(userAnswers) match {
        case Some(onwardRoute) => onwardRoute
        case None =>
          logger.error(s"No navigation defined for $page")
          routes.ErrorController.technicalDifficulties()
      }

    mode match {
      case NormalMode =>
        normalRoutes(departureId, mode).lift(page) match {
          case None       => controllers.routes.IndexController.index(departureId)
          case Some(call) => handleCall(call)
        }
      case CheckMode =>
        checkRoutes(departureId, mode).lift(page) match {
          case None       => controllers.routes.IndexController.index(departureId)
          case Some(call) => handleCall(call)
        }
    }
  }
}
