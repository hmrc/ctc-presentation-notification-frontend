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
import navigation.LocationOfGoodsNavigator
import pages.locationOfGoods.UnLocodePage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.UnLocodeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.UnLocodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnLocodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  formProvider: UnLocodeFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  navigator: LocationOfGoodsNavigator,
  view: UnLocodeView,
  service: UnLocodeService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "locationOfGoods.unLocode"
  private val form   = formProvider(prefix)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val preparedForm = request.userAnswers
        .get(UnLocodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(view(preparedForm, departureId, mode)))
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val boundForm = form.bindFromRequest()
      boundForm
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode))),
          value =>
            service.doesUnLocodeExist(value).flatMap {
              case true =>
                redirect(mode, value, departureId)
              case false =>
                val formWithErrors = boundForm.withError(FormError("value", s"$prefix.error.not.exists"))
                Future.successful(BadRequest(view(formWithErrors, departureId, mode)))
            }
        )
  }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(UnLocodePage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(UnLocodePage, updatedAnswers, departureId, mode))
}
