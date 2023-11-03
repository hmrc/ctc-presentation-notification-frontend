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
import forms.SelectableFormProvider
import models.reference.CustomsOffice
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.BorderNavigator
import pages.transport.border.active.CustomsOfficeActiveBorderPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.CustomsOfficesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.border.active.CustomsOfficeActiveBorderView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeActiveBorderController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: BorderNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  customsOfficeService: CustomsOfficesService,
  val controllerComponents: MessagesControllerComponents,
  view: CustomsOfficeActiveBorderView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val customsOfficeList         = customsOfficeService.getCustomsOffices(request.userAnswers)
      val form: Form[CustomsOffice] = formProvider("transport.border.active.customsOfficeActiveBorder", customsOfficeList)
      val preparedForm = request.userAnswers.get(CustomsOfficeActiveBorderPage(activeIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, departureId, customsOfficeList.values, mode, activeIndex))
  }

  def onSubmit(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val customsOfficeList         = customsOfficeService.getCustomsOffices(request.userAnswers)
      val form: Form[CustomsOffice] = formProvider("transport.border.active.customsOfficeActiveBorder", customsOfficeList)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, customsOfficeList.values, mode, activeIndex))),
          value => redirect(mode, value, departureId, activeIndex)
        )
  }

  private def redirect(
    mode: Mode,
    value: CustomsOffice,
    departureId: String,
    activeIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomsOfficeActiveBorderPage(activeIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(CustomsOfficeActiveBorderPage(activeIndex), updatedAnswers, departureId, mode))

}