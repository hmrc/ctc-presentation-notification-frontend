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

package controllers.transport.equipment

import controllers.actions._
import forms.SelectableFormProvider
import models.{Index, Mode}
import navigation.Navigator
import pages.transport.equipment.ItemPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.SelectItemsViewModel
import views.html.transport.equipment.SelectItemsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectItemsController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelectItemsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val getSelectedItem = request.userAnswers.get(ItemPage(equipmentIndex, itemIndex))
      val viewModel       = SelectItemsViewModel(request.userAnswers, getSelectedItem)
      val form            = formProvider("transport.equipment.selectItems", viewModel.items)
      val preparedForm = getSelectedItem match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, equipmentIndex, itemIndex, departureId, viewModel, mode))
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val getSelectedItem = request.userAnswers.get(ItemPage(equipmentIndex, itemIndex))
      val viewModel       = SelectItemsViewModel(request.userAnswers, getSelectedItem)

      val form = formProvider("transport.equipment.selectItems", viewModel.items)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, equipmentIndex, itemIndex, departureId, viewModel, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ItemPage(equipmentIndex, itemIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode))
        )
  }
}
