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
import pages.transport.border.active._
import pages.transport.border._
import controllers.transport.border.active.routes
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class BorderNavigator @Inject() (activeIndex: Index) extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {

    case BorderModeOfTransportPage => ua => identificationPageNavigation(ua, departureId, mode, activeIndex)
    case IdentificationPage(activeIndex) => ua => IdentificationNumberPage(activeIndex).route(ua,departureId,mode)
    case IdentificationNumberPage(activeIndex) => ua => NationalityPage(activeIndex).route(ua, departureId, mode)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  private def identificationPageNavigation(ua: UserAnswers, departureId: String, mode: Mode, activeIndex: Index): Option[Call] = {
    (ua.departureData.Consignment.modeOfTransportAtTheBorder,
      ua.departureData.TransitOperation.security,
      ua.departureData.Consignment.ActiveBorderTransportMeans.isDefined
      ) match {
      case (Some("5"), 0, true) => ???
      case _                    => Some(routes.IdentificationController.onPageLoad(departureId, mode, activeIndex))
    }
  }

}
