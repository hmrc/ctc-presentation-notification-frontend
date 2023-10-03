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
import forms.UnLocodeFormProvider
import models.Mode
import models.requests.MandatoryDataRequest
import navigation.Navigator
import pages.UnLocodePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.UnLocodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnLocodeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  formProvider: UnLocodeFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  navigator: Navigator,
  view: UnLocodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("locationOfGoods.unLocode")

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(UnLocodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, departureId, request.userAnswers.lrn, mode))
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, request.userAnswers.lrn, mode))),
            value => redirect(mode, value, departureId)
          )

    }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(UnLocodePage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(UnLocodePage, updatedAnswers, departureId, mode))
}
