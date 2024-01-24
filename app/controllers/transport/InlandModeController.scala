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

package controllers.transport

import controllers.actions._
import forms.EnumerableFormProvider
import models.reference.TransportMode.InlandMode
import models.requests.MandatoryDataRequest
import models.Mode
import navigation.BorderNavigator
import pages.QuestionPage
import pages.transport.InlandModePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransportModeCodesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.InlandModeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InlandModeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  navigator: BorderNavigator,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: InlandModeView,
  service: TransportModeCodesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(inlandModeCodes: Seq[InlandMode]): Form[InlandMode] = formProvider[InlandMode]("transport.inlandModeOfTransport", inlandModeCodes)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getInlandModes().map {
        inlandModeCodes =>
          val inlandMode =
            request.userAnswers
              .get(InlandModePage)
              .map(_.code)
              .orElse {
                request.userAnswers.departureData.Consignment.inlandModeOfTransport
              }

          val preparedForm = inlandMode match {
            case None => form(inlandModeCodes)
            case Some(value) =>
              val getInlandMode = inlandModeCodes.find(_.code == value)
              getInlandMode match {
                case Some(inlandMode) => form(inlandModeCodes).fill(inlandMode)
                case None             => form(inlandModeCodes)
              }
          }

          Ok(view(preparedForm, departureId, inlandModeCodes, mode))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getInlandModes().flatMap {
        inlandModeCodes =>
          form(inlandModeCodes)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, inlandModeCodes, mode))),
              value => redirect(mode, InlandModePage, value, departureId)
            )
      }
  }

  private def redirect(
    mode: Mode,
    page: QuestionPage[InlandMode],
    value: InlandMode,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))
}
