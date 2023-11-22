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
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.ApplyAnotherItemViewModel
import viewModels.transport.equipment.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider
import views.html.transport.equipment.ApplyAnotherItemView

import javax.inject.Inject

class ApplyAnotherItemController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ApplyAnotherItemView,
  viewModelProvider: ApplyAnotherItemViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: ApplyAnotherItemViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = {
    println("\n\n\n\n")
    println("HERE")
    actions.requireData(departureId) {
      implicit request =>
        val viewModel = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex)
        viewModel.count match {
          case 0 =>
            Redirect(routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, Index(0)))
          case _ => Ok(view(form(viewModel), departureId, viewModel))
        }
    }
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, departureId, viewModel)),
          {
            case true =>
              Redirect(routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, viewModel.nextIndex))
            case false =>
              Redirect(routes.AddAnotherEquipmentController.onPageLoad(departureId, mode))
          }
        )
  }
}