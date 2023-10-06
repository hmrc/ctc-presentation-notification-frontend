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
import pages._
import pages.locationOfGoods.CoordinatesPage
import play.api.mvc.Call

@Singleton
class Navigator {

  protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case InferredLocationTypePage | LocationTypePage => ua => IdentificationPage.route(ua, departureId, mode)
    case CoordinatesPage                             => ???
    case InferredLocationTypePage                    => ua => ???
    case CoordinatesPage                             => ???
    case EoriPage                                    => ua => ???
  }

  private def handleCall(userAnswers: UserAnswers, call: UserAnswers => Option[Call]) =
    call(userAnswers) match {
      case Some(onwardRoute) => onwardRoute
      case None              => ??? //TODO add error page
    }

  def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call =
    normalRoutes(departureId, mode).lift(page) match {
      case None       => controllers.routes.IndexController.index(departureId)
      case Some(call) => handleCall(userAnswers, call)
    }
}
