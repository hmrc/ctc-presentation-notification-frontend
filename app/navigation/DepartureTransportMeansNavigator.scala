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
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage, TransportMeansNationalityPage}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class DepartureTransportMeansNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ => _ => None
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {

    case TransportMeansIdentificationPage(transportIndex)       => ua => transportMeansIdentificationNavigation(ua, departureId, mode, transportIndex)
    case TransportMeansIdentificationNumberPage(transportIndex) => ua => transportMeansNumberNavigation(ua, departureId, mode, transportIndex)
    case TransportMeansNationalityPage(_)                       => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

  }

  private def transportMeansIdentificationNavigation(ua: UserAnswers, departureId: String, mode: Mode, transportIndex: Index): Option[Call] =
    ua.get(TransportMeansIdentificationNumberPage(transportIndex)) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None =>
        Some(controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController.onPageLoad(departureId, mode, transportIndex))
    }

  private def transportMeansNumberNavigation(ua: UserAnswers, departureId: String, mode: Mode, transportIndex: Index): Option[Call] =
    ua.get(TransportMeansNationalityPage(transportIndex)) match {
      case Some(_) => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      case None    => Some(controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController.onPageLoad(departureId, mode, transportIndex))
    }

}
