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
import forms.YesNoFormProvider
import models.{CheckMode, Mode}
import models.requests.MandatoryDataRequest
import navigation.LoadingNavigator
import pages.loading.AddExtraInformationYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.loading.AddExtraInformationYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddExtraInformationYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: LoadingNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddExtraInformationYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("loading.addExtraInformationYesNo")

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddExtraInformationYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, departureId, mode))
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode))),
          value => {
            val isCYANext = if (mode == CheckMode) isNextPageCYA(departureId) else false
            redirect(mode, value, departureId, isCYANext)
          }
        )
  }

  private def isNextPageCYA(
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Boolean = {
    val nextPage = navigator.nextPage(AddExtraInformationYesNoPage, request.userAnswers, departureId, CheckMode)
    nextPage == controllers.routes.CheckYourAnswersController.onPageLoad(departureId)
  }

  private def redirect(
    mode: Mode,
    value: Boolean,
    departureId: String,
    isCYAPage: Boolean
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddExtraInformationYesNoPage, value))
      _              <- if ((mode != CheckMode) || isCYAPage) sessionRepository.set(updatedAnswers) else Future.unit
    } yield Redirect(navigator.nextPage(AddExtraInformationYesNoPage, updatedAnswers, departureId, mode))
}
