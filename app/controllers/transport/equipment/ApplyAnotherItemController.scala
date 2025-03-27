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
import controllers.actions.*
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.GoodsReferenceGroupNavigator.GoodsReferenceGroupNavigatorProvider
import pages.transport.equipment.index.ApplyAnotherItemPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.equipment.ApplyAnotherItemViewModel.ApplyAnotherItemViewModelProvider
import viewModels.transport.equipment.{ApplyAnotherItemViewModel, SelectItemsViewModel}
import views.html.transport.equipment.ApplyAnotherItemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplyAnotherItemController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ApplyAnotherItemView,
  viewModelProvider: ApplyAnotherItemViewModelProvider,
  sessionRepository: SessionRepository,
  navigatorProvider: GoodsReferenceGroupNavigatorProvider
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: ApplyAnotherItemViewModel, equipmentIndex: Index): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore, equipmentIndex.display)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val isNumberItemsZero: Boolean = SelectItemsViewModel(request.userAnswers).items.values.isEmpty
      val viewModel                  = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex, isNumberItemsZero)
      viewModel.count match {
        case 0 =>
          Redirect(routes.SelectItemsController.onPageLoad(departureId, mode, equipmentIndex, Index(0)))
        case _ => Ok(view(form(viewModel, equipmentIndex), viewModel))
      }
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val isNumberItemsZero: Boolean = SelectItemsViewModel(request.userAnswers).items.values.isEmpty
      val viewModel                  = viewModelProvider(request.userAnswers, departureId, mode, equipmentIndex, isNumberItemsZero)
      form(viewModel, equipmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, viewModel))),
          value => redirect(departureId, mode, value, equipmentIndex, viewModel.nextIndex)
        )
  }

  private def redirect(
    departureId: String,
    mode: Mode,
    value: Boolean,
    equipmentIndex: Index,
    nextGoodsReferenceIndex: Index
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(ApplyAnotherItemPage(equipmentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val navigator = navigatorProvider.apply(nextGoodsReferenceIndex)
      Redirect(navigator.nextPage(ApplyAnotherItemPage(equipmentIndex), updatedAnswers, departureId, mode))
    }

}
