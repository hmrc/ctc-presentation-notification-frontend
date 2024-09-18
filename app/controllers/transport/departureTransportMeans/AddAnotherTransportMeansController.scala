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

package controllers.transport.departureTransportMeans

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode, NormalMode}
import navigation.DepartureTransportMeansNavigator
import pages.transport.departureTransportMeans.AddAnotherTransportMeansPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel.AddAnotherTransportMeansViewModelProvider
import views.html.transport.departureTransportMeans.AddAnotherTransportMeansView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherTransportMeansController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherTransportMeansView,
  viewModelProvider: AddAnotherTransportMeansViewModelProvider,
  navigator: DepartureTransportMeansNavigator
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherTransportMeansViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode)
      viewModel.count match {
        case 0 => Redirect(routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, Index(0)))
        case _ => Ok(view(form(viewModel), viewModel))
      }
  }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, departureId, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, viewModel))),
          value => redirect(departureId, mode, value, viewModel.nextIndex)
        )
  }

  private def redirect(
    departureId: String,
    mode: Mode,
    value: Boolean,
    transportIndex: Index
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnotherTransportMeansPage(transportIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddAnotherTransportMeansPage(transportIndex), updatedAnswers, departureId, mode))

}
