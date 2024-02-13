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

package controllers.loading

import controllers.actions._
import forms.SelectableFormProvider
import models.Mode
import models.reference.Country
import models.requests.MandatoryDataRequest
import navigation.LoadingNavigator
import pages.loading.CountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.loading.CountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionRepository: SessionRepository,
  navigator: LoadingNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      countriesService.getCountries().map {
        countryList =>
          val form = formProvider("loading.country", countryList)
          val preparedForm = request.userAnswers.get(CountryPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, departureId, countryList.values, mode))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      countriesService.getCountries().flatMap {
        countryList =>
          val form = formProvider("loading.country", countryList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, countryList.values, mode))),
              value => redirect(mode, value, departureId)
            )
      }
  }

  private def redirect(
    mode: Mode,
    value: Country,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(CountryPage, updatedAnswers, departureId, mode))

}
