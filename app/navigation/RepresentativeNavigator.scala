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
import pages.representative._
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class RepresentativeNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case ActingAsRepresentativePage               => ua => actingAsRepresentativeCheckRoute(ua, departureId)
    case AddRepresentativeContactDetailsYesNoPage => ua => addRepresentativeContactDetailsCheckRoute(ua, departureId)
    case NamePage                                 => ua => namePageCheckRoute(ua, departureId)
    case RepresentativePhoneNumberPage            => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
  }

  private def actingAsRepresentativeCheckRoute(ua: UserAnswers, departureId: String): Option[Call] =
    Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

  private def addRepresentativeContactDetailsCheckRoute(ua: UserAnswers, departureId: String): Option[Call] = {
    val isContactDetailsDefined = ua.departureData.Representative.map(_.ContactPerson.isDefined)

    (ua.get(AddRepresentativeContactDetailsYesNoPage), isContactDetailsDefined) match {
      case (Some(true), Some(false)) => NamePage.route(ua, departureId, CheckMode)
      case _                         => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

  private def namePageCheckRoute(ua: UserAnswers, departureId: String): Option[Call] = {
    val isContactDetailsDefined = ua.departureData.Representative.map(_.ContactPerson.isDefined)

    isContactDetailsDefined match {
      case Some(false) => RepresentativePhoneNumberPage.route(ua, departureId, CheckMode)
      case _           => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }
}
