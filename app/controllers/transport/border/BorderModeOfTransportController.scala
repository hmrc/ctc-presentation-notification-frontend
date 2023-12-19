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

package controllers.transport.border

import controllers.actions._
import forms.EnumerableFormProvider
import models.Mode
import models.reference.TransportMode.BorderMode
import models.requests.MandatoryDataRequest
import navigation.BorderNavigator
import pages.QuestionPage
import pages.transport.border.BorderModeOfTransportPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransportModeCodesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.border.BorderModeOfTransportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BorderModeOfTransportController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: BorderNavigator,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BorderModeOfTransportView,
  service: TransportModeCodesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(borderModeCodes: Seq[BorderMode]): Form[BorderMode] = formProvider[BorderMode]("transport.border.borderModeOfTransport", borderModeCodes)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getBorderModes().map {
        borderModeCodes =>
          val preparedForm = request.userAnswers.get(BorderModeOfTransportPage) match {
            case None        => form(borderModeCodes)
            case Some(value) => form(borderModeCodes).fill(value)
          }

          Ok(view(preparedForm, departureId, borderModeCodes, mode))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getBorderModes().flatMap {
        borderModeCodes =>
          form(borderModeCodes)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, borderModeCodes, mode))),
              value => redirect(mode, BorderModeOfTransportPage, value, departureId)
            )
      }
  }

  private def redirect(
    mode: Mode,
    page: QuestionPage[BorderMode],
    value: BorderMode,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))

}
