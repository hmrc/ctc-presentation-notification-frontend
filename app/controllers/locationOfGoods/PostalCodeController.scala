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
import forms.locationOfGoods.PostalCodeFormProvider
import models.requests.MandatoryDataRequest
import models.{Mode, PostalCodeAddress}
import navigation.LocationOfGoodsNavigator
import pages.locationOfGoods.PostalCodePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.PostalCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostalCodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  actions: Actions,
  formProvider: PostalCodeFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: PostalCodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val prefix: String = "locationOfGoods.postalCode"

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        countriesService.getAddressPostcodeBasedCountries().map {
          countryList =>
            val postalCode = request.userAnswers
              .get(PostalCodePage)
            val form = formProvider(prefix, countryList)
            val preparedForm = postalCode match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, departureId, mode, countryList.values))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        countriesService.getAddressPostcodeBasedCountries().flatMap {
          countryList =>
            val form = formProvider(prefix, countryList)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, countryList.values))),
                value => redirect(mode, value, departureId)
              )

        }
    }

  private def redirect(
    mode: Mode,
    value: PostalCodeAddress,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(PostalCodePage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(PostalCodePage, updatedAnswers, departureId, mode))
}
