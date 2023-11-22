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

package controllers.transport.equipment.index

import controllers.actions._
import forms.YesNoFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.EquipmentNavigator
import pages.transport.equipment.index.AddSealYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.index.AddSealYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSealYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: EquipmentNavigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddSealYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider("transport.equipment.index.addSealYesNo")

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireData(departureId) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddSealYesNoPage(equipmentIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, departureId, mode, equipmentIndex))
    }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex))),
            value => redirect(mode, value, departureId, equipmentIndex)
          )
    }

  private def redirect(
    mode: Mode,
    value: Boolean,
    departureId: String,
    equipmentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddSealYesNoPage(equipmentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddSealYesNoPage(equipmentIndex), updatedAnswers, departureId, mode))
}
