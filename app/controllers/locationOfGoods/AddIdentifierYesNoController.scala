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
import forms.YesNoFormProvider
import models.Mode
import models.requests.MandatoryDataRequest
import navigation.LocationOfGoodsNavigator
import pages.locationOfGoods.AddIdentifierYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.AddIdentifierYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddIdentifierYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddIdentifierYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider("locationOfGoods.addIdentifierYesNo")

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val identifierYesNo =
        request.userAnswers
          .get(AddIdentifierYesNoPage)
      val preparedForm = identifierYesNo match {
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
          value => redirect(mode, value, departureId)
        )
  }

  private def redirect(
    mode: Mode,
    value: Boolean,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddIdentifierYesNoPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddIdentifierYesNoPage, updatedAnswers, departureId, mode))
}
