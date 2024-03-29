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

package controllers.locationOfGoods

import controllers.actions._
import controllers.locationOfGoods.AddressController.getCountryCode
import forms.DynamicAddressFormProvider
import models.reference.CountryCode
import models.requests.{DataRequest, MandatoryDataRequest}
import models.{DynamicAddress, Mode}
import navigation.LocationOfGoodsNavigator
import pages.QuestionPage
import pages.locationOfGoods.{AddressPage, CountryPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.AddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  actions: Actions,
  formProvider: DynamicAddressFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: AddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        getCountryCode match {
          case Some(country) =>
            countriesService.doesCountryRequireZip(country).map {
              isPostalCodeRequired =>
                val getCountry = request.userAnswers
                  .get(AddressPage)
                val preparedForm = getCountry match {
                  case None        => form(isPostalCodeRequired)
                  case Some(value) => form(isPostalCodeRequired).fill(value)
                }

                Ok(view(preparedForm, departureId, mode, isPostalCodeRequired))
            }
          case None => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
    }

  private def form(isPostalCodeRequired: Boolean)(implicit request: MandatoryDataRequest[_]): Form[DynamicAddress] =
    formProvider("locationOfGoods.address", isPostalCodeRequired)(request.request.messages(messagesApi))

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        getCountryCode match {
          case Some(country) =>
            countriesService.doesCountryRequireZip(country).flatMap {
              isPostalCodeRequired =>
                form(isPostalCodeRequired)
                  .bindFromRequest()
                  .fold(
                    formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, isPostalCodeRequired))),
                    value => redirect(mode, AddressPage, value, departureId)
                  )
            }
          case None => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }

    }

  private def redirect(
    mode: Mode,
    page: QuestionPage[DynamicAddress],
    value: DynamicAddress,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))
}

object AddressController extends Logging {

  private[locationOfGoods] def getCountryCode(implicit request: DataRequest[AnyContent]): Option[CountryCode] =
    request.userAnswers
      .get(CountryPage)
      .map(_.code)
}
