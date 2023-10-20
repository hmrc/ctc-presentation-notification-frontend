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
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class LocationOfGoodsNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case InferredLocationTypePage | LocationTypePage                                              => ua => IdentificationPage.route(ua, departureId, mode)
    case IdentificationPage                                                                       => ua => routeIdentificationPageNavigation(ua, departureId, mode)
    case CountryPage                                                                              => ua => AddressPage.route(ua, departureId, mode)
    case MoreInformationPage                                                                      => ua => locationOfGoodsNavigation(ua, departureId, mode)
    case EoriPage | AuthorisationNumberPage                                                       => ua => AddIdentifierYesNoPage.route(ua, departureId, mode)
    case AddIdentifierYesNoPage                                                                   => ua => addIdentifierYesNoNavigation(ua, departureId, mode)
    case AdditionalIdentifierPage | CoordinatesPage | UnLocodePage | AddressPage | PostalCodePage => ua => AddContactYesNoPage.route(ua, departureId, mode)
    case AddContactYesNoPage                                                                      => ua => addContactYesNoNavigation(ua, departureId, mode)
    case NamePage                                                                                 => ua => PhoneNumberPage.route(ua, departureId, mode)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  def routeIdentificationPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(IdentificationPage).map {
      case ltp if ltp.code == CustomsOfficeIdentifier       => controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode)
      case ltp if ltp.code == EoriNumberIdentifier          => controllers.locationOfGoods.routes.EoriController.onPageLoad(departureId, mode)
      case ltp if ltp.code == AuthorisationNumberIdentifier => controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode)
      case ltp if ltp.code == CoordinatesIdentifier         => controllers.locationOfGoods.routes.CoordinatesController.onPageLoad(departureId, mode)
      case ltp if ltp.code == UnlocodeIdentifier            => controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode)
      case ltp if ltp.code == AddressIdentifier             => controllers.locationOfGoods.routes.CountryController.onPageLoad(departureId, mode)
      case ltp if ltp.code == PostalCodeIdentifier          => controllers.locationOfGoods.routes.PostalCodeController.onPageLoad(departureId, mode)
    }

  def locationOfGoodsNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] = {
    val nextPage = ua.departureData.Consignment.LocationOfGoods match {
      case None if !ua.departureData.isSimplified => Some(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode))
      case None                                   => Some(controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode))
      case Some(_)                                => ???
    }
    nextPage
  }

  def addIdentifierYesNoNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(AddIdentifierYesNoPage) match {
      case Some(true)  => AdditionalIdentifierPage.route(userAnswers, departureId, mode)
      case Some(false) => AddContactYesNoPage.route(userAnswers, departureId, mode)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  def addContactYesNoNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(AddContactYesNoPage) match {
      case Some(true)  => NamePage.route(userAnswers, departureId, mode)
      case Some(false) => ???
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

}
