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

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.EquipmentNavigator
import pages.transport.equipment.index.AddAnotherSealPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.AddAnotherSealViewModel
import viewModels.transport.equipment.AddAnotherSealViewModel.AddAnotherSealViewModelProvider
import views.html.transport.equipment.index.AddAnotherSealView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherSealController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  navigator: EquipmentNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherSealView,
  viewModelProvider: AddAnotherSealViewModelProvider
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherSealViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex)
      viewModel.count match {
        case 0 => Redirect(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))
        case _ => Ok(view(form(viewModel), viewModel))
      }
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, viewModel))),
          value => redirect(mode, value, departureId, equipmentIndex, viewModel.nextIndex)
        )
  }

  private def redirect(
    mode: Mode,
    value: Boolean,
    departureId: String,
    equipmentIndex: Index,
    sealIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherSealPage(equipmentIndex, sealIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddAnotherSealPage(equipmentIndex, sealIndex), updatedAnswers, departureId, mode))

}
