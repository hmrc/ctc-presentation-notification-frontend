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
import controllers.transport.equipment.index.seals.{routes => sealRoutes}
import controllers.transport.equipment.index.{routes => indexRoutes}
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import pages.sections.transport.equipment.{EquipmentsSection, SealsSection}
import pages.transport.ContainerIndicatorPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.AddAnotherEquipmentViewModel
import viewModels.transport.equipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider
import views.html.transport.equipment.AddAnotherEquipmentView

import javax.inject.Inject

class AddAnotherEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherEquipmentView,
  viewModelProvider: AddAnotherEquipmentViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherEquipmentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode)
      viewModel.count match {
        case 0 => Redirect(routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode))
        case _ => Ok(view(form(viewModel), departureId, viewModel))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, departureId, viewModel)),
          {
            case true =>
              redirect(departureId, mode, viewModel)
            case false =>
              Redirect(Call("GET", "#")) //TODO: redirect to CYA
          }
        )
  }

  private def redirect(
    departureId: String,
    mode: Mode,
    equipmentViewModel: AddAnotherEquipmentViewModel
  )(implicit request: MandatoryDataRequest[_]): Result = {

    val sealIndex = request.userAnswers.get(SealsSection(equipmentViewModel.nextIndex)).map(_.value.length).getOrElse(0)

    (request.userAnswers.get(ContainerIndicatorPage), request.userAnswers.get(EquipmentsSection).isDefined) match {
      case (Some(true), true) =>
        Redirect(indexRoutes.AddContainerIdentificationNumberYesNoController.onPageLoad(departureId, mode, equipmentViewModel.nextIndex))
      case _ if request.userAnswers.departureData.isSimplified && request.userAnswers.departureData.hasAuthC523 =>
        Redirect(sealRoutes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentViewModel.nextIndex, Index(sealIndex)))
      case _ => Redirect(indexRoutes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentViewModel.nextIndex))
    }
  }
}
