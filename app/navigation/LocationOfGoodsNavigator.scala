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
import navigation.LoadingNavigator._
import pages._
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.transport.{CheckInformationPage, ContainerIndicatorPage, LimitDatePage}
import play.api.mvc.Call

import javax.inject.Inject

@Singleton
class LocationOfGoodsNavigator @Inject() () extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case InferredLocationTypePage | LocationTypePage                                              => ua => IdentificationPage.route(ua, departureId, mode)
    case IdentificationPage                                                                       => ua => routeIdentificationPageNavigation(ua, departureId, mode)
    case CountryPage                                                                              => ua => AddressPage.route(ua, departureId, mode)
    case MoreInformationPage                                                                      => ua => locationOfGoodsNavigation(ua, departureId, mode)
    case CheckInformationPage                                                                     => ua => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    case EoriPage | AuthorisationNumberPage                                                       => ua => AddIdentifierYesNoPage.route(ua, departureId, mode)
    case AddIdentifierYesNoPage                                                                   => ua => addIdentifierYesNoNavigation(ua, departureId, mode)
    case AdditionalIdentifierPage | CoordinatesPage | UnLocodePage | AddressPage | PostalCodePage => ua => AddContactYesNoPage.route(ua, departureId, mode)
    case AddContactYesNoPage                                                                      => ua => addContactYesNoNavigation(ua, departureId, mode)
    case NamePage                                                                                 => ua => PhoneNumberPage.route(ua, departureId, mode)
    case CustomsOfficeIdentifierPage                                                              => ua => placeOfLoadingExistsRedirect(ua, departureId, mode)
    case PhoneNumberPage                                                                          => ua => phoneNumberPageNavigation(ua, departureId, mode)
    case LimitDatePage                                                                            => ua => limitDatePageNavigator(departureId, mode, ua)
  }

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case LimitDatePage                                 => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    case InferredIdentificationPage | LocationTypePage => ua => IdentificationPage.route(ua, departureId, mode)
    case AddIdentifierYesNoPage                        => ua => addIdentifierYesNoNavigation(ua, departureId, mode)
    case EoriPage | AuthorisationNumberPage =>
      ua => AddIdentifierYesNoPage.route(ua, departureId, mode)
    case AdditionalIdentifierPage | CoordinatesPage | UnLocodePage | AddressPage | PostalCodePage | CustomsOfficeIdentifierPage =>
      ua => AddContactYesNoPage.route(ua, departureId, mode)
    case IdentificationPage  => ua => routeIdentificationPageNavigation(ua, departureId, mode)
    case AddContactYesNoPage => ua => addContactYesNoNavigation(ua, departureId, mode)
    case NamePage            => ua => namePageNavigation(ua, departureId, mode)
    case PhoneNumberPage     => ua => phoneNumberPageNavigation(ua, departureId, mode)
    case CountryPage         => ua => AddressPage.route(ua, departureId, mode)
  }

  def namePageNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    (ua.get(PhoneNumberPage).isEmpty, ua.get(AddContactYesNoPage).contains(true)) match {
      case (true, true) => Some(controllers.locationOfGoods.contact.routes.PhoneNumberController.onPageLoad(departureId, mode))
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  def routeIdentificationPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(IdentificationPage).map {
      case ltp if ltp.code == CustomsOfficeIdentifier       => controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode)
      case ltp if ltp.code == EoriNumberIdentifier          => controllers.locationOfGoods.routes.EoriController.onPageLoad(departureId, mode)
      case ltp if ltp.code == AuthorisationNumberIdentifier => controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode)
      case ltp if ltp.code == CoordinatesIdentifier         => controllers.locationOfGoods.routes.CoordinatesController.onPageLoad(departureId, mode)
      case ltp if ltp.code == UnlocodeIdentifier            => controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode)
      case ltp if ltp.code == AddressIdentifier             => controllers.locationOfGoods.routes.CountryController.onPageLoad(departureId, mode)
      case _                                                => controllers.locationOfGoods.routes.PostalCodeController.onPageLoad(departureId, mode)
    }

  private def limitDatePageNavigator(departureId: String, mode: Mode, ua: UserAnswers) =
    ua.get(ContainerIndicatorPage) match {
      case Some(_) =>
        containerIndicatorPageNavigation(departureId, mode, ua)
      case None => ContainerIndicatorPage.route(ua, departureId, mode)
    }

  def locationOfGoodsNavigation(ua: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    ua.departureData.Consignment.LocationOfGoods match {
      case None if !ua.departureData.isSimplified => Some(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode))
      case None                                   => Some(controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode))
      case Some(_) =>
        placeOfLoadingExistsRedirect(ua, departureId, mode)
    }

  private def addIdentifierYesNoNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.get(AddIdentifierYesNoPage) match {
      case Some(true)  => AdditionalIdentifierPage.route(userAnswers, departureId, mode)
      case Some(false) => AddContactYesNoPage.route(userAnswers, departureId, mode)
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def addContactYesNoNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        userAnswers.get(AddContactYesNoPage) match {
          case Some(true)  => NamePage.route(userAnswers, departureId, mode)
          case Some(false) => placeOfLoadingExistsRedirect(userAnswers, departureId, mode)
          case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
        }
      case CheckMode =>
        userAnswers.get(AddContactYesNoPage) match {
          case Some(true) if userAnswers.get(NamePage).isEmpty => NamePage.route(userAnswers, departureId, mode)
          case _                                               => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
    }

  private def phoneNumberPageNavigation(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    mode match {
      case NormalMode =>
        userAnswers.departureData.Consignment.PlaceOfLoading match {
          case Some(_) => placeOfLoadingExistsRedirect(userAnswers, departureId, mode)
          case None    => AddUnLocodePage.route(userAnswers, departureId, mode)
        }
      case CheckMode => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  private def placeOfLoadingExistsRedirect(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    userAnswers.departureData.Consignment.PlaceOfLoading match {
      case Some(_) => locationPageNavigation(departureId, mode, userAnswers)

      case None => AddUnLocodePage.route(userAnswers, departureId, mode)
    }
}
