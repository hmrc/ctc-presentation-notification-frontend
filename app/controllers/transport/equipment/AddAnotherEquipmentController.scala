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

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{CheckMode, Index, Mode}
import navigation.EquipmentNavigator
import pages.transport.equipment.AddAnotherTransportEquipmentPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.{AddAnotherEquipmentViewModel, SelectItemsViewModel}
import viewModels.transport.equipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider
import views.html.transport.equipment.AddAnotherEquipmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherEquipmentView,
  viewModelProvider: AddAnotherEquipmentViewModelProvider,
  navigator: EquipmentNavigator
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherEquipmentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val isNumberItemsZero: Boolean = SelectItemsViewModel(request.userAnswers).items.values.isEmpty
      val viewModel                  = viewModelProvider(request.userAnswers, departureId, mode, isNumberItemsZero)
      viewModel.count match {
        case 0 => Redirect(routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode))
        case _ => Ok(view(form(viewModel), departureId, viewModel))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val isNumberItemsZero: Boolean = SelectItemsViewModel(request.userAnswers).items.values.isEmpty
      val viewModel                  = viewModelProvider(request.userAnswers, departureId, mode, isNumberItemsZero)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, viewModel))),
          value => redirect(departureId, mode, value, viewModel.nextIndex)
        )
  }

  private def redirect(
    departureId: String,
    mode: Mode,
    value: Boolean,
    activeIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherTransportEquipmentPage(activeIndex), value))
      _              <- if (mode != CheckMode) sessionRepository.set(updatedAnswers) else Future.unit
    } yield Redirect(navigator.nextPage(AddAnotherTransportEquipmentPage(activeIndex), updatedAnswers, departureId, mode))

}
