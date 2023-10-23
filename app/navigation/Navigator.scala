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
import config.Constants._
import models._
import pages._
import pages.locationOfGoods._
import pages.locationOfGoods.contact.PhoneNumberPage
import play.api.mvc.Call

@Singleton
class Navigator {

  protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case InferredLocationTypePage | LocationTypePage => ua => IdentificationPage.route(ua, departureId, mode)
    case IdentificationPage                          => ua => routeIdentificationPageNavigation(ua, departureId, mode)
    case CountryPage                                 => ua => AddressPage.route(ua, departureId, mode)
    case MoreInformationPage                         => ua => locationOfGoodsNavigation(ua, departureId, mode)
    case CustomsOfficeIdentifierPage                 => ua => AddUnLocodePage.route(ua, departureId, mode)
    case AddContactYesNoPage                         => ua => addContactYesNoPageNavigation(ua, departureId, mode)
    case PhoneNumberPage                             => ua => phoneNumberPageNavigation(ua, departureId, mode)
    case CoordinatesPage                             => ua => ???
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

  def routeIdentificationPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(IdentificationPage).flatMap {
      case ltp if ltp.code == CustomsOfficeIdentifier       => CustomsOfficeIdentifierPage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == EoriNumberIdentifier          => EoriPage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == AuthorisationNumberIdentifier => AuthorisationNumberPage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == CoordinatesIdentifier         => CoordinatesPage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == UnlocodeIdentifier            => UnLocodePage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == AddressIdentifier             => CountryPage.route(userAnswers, departureId, mode)
      case ltp if ltp.code == PostalCodeIdentifier          => PostalCodePage.route(userAnswers, departureId, mode)
    }

  def addContactYesNoPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] = userAnswers.get(AddContactYesNoPage).flatMap {
    addContact =>
      if (addContact) ??? else AddUnLocodePage.route(userAnswers, departureId, mode)
  }

  def phoneNumberPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.departureData.Consignment.PlaceOfLoading match {
      case Some(_) => AddUnLocodePage.route(userAnswers, departureId, mode)
      case None    => ???
    }

  def locationOfGoodsNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.departureData.Consignment.LocationOfGoods match {
      case None if !userAnswers.departureData.isSimplified => LocationTypePage.route(userAnswers, departureId, mode)
      case None                                            => AuthorisationNumberPage.route(userAnswers, departureId, mode)
      case Some(_)                                         => AddUnLocodePage.route(userAnswers, departureId, mode)
    }
}
