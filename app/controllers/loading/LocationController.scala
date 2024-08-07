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
import forms.Constants.loadingLocationMaxLength
import forms.loading.LoadingLocationFormProvider
import models.Mode
import models.requests.{DataRequest, MandatoryDataRequest}
import navigation.LoadingNavigator
import pages.loading.{CountryPage, LocationPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.loading.LocationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LoadingNavigator,
  formProvider: LoadingLocationFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: LocationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def countryName(implicit request: DataRequest[_]): String =
    request.userAnswers.get(CountryPage).map(_.description).getOrElse("")

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val form = formProvider("loading.location", countryName)
      val preparedForm = request.userAnswers.get(LocationPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, departureId, countryName, loadingLocationMaxLength, mode))
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val form = formProvider("loading.location", countryName)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, countryName, loadingLocationMaxLength, mode))),
          value => redirect(mode, value, departureId)
        )
  }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(LocationPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(LocationPage, updatedAnswers, departureId, mode))
}
