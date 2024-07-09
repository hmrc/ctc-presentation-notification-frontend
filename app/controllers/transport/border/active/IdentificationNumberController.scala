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

package controllers.transport.border.active

import controllers.actions._
import forms.border.IdentificationNumberFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.BorderNavigator
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.border.active.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: BorderNavigator,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "transport.border.active.identificationNumber"

  private val form = formProvider(prefix)

  def onPageLoad(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .andThen(getMandatoryPage(IdentificationPage(activeIndex))) {
        implicit request =>
          val preparedForm = request.userAnswers.get(IdentificationNumberPage(activeIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, departureId, mode, activeIndex, request.arg.asString))
      }

  def onSubmit(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .andThen(getMandatoryPage(IdentificationPage(activeIndex)))
      .async {
        implicit request =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, activeIndex, request.arg.asString))),
              value => redirect(mode, value, departureId, activeIndex)
            )
      }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String,
    activeIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(IdentificationNumberPage(activeIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(IdentificationNumberPage(activeIndex), updatedAnswers, departureId, mode))
}
