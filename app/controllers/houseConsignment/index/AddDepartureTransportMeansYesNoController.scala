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

package controllers.houseConsignment.index

import controllers.actions._
import forms.YesNoFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.AddDepartureTransportMeansYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddDepartureTransportMeansYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddDepartureTransportMeansYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(houseConsignmentIndex: Index) = formProvider("houseConsignment.index.addDepartureTransportMeansYesNo", houseConsignmentIndex.display)

  def onPageLoad(departureId: String, mode: Mode, houseConsignmentIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex)) match {
        case None        => form(houseConsignmentIndex)
        case Some(value) => form(houseConsignmentIndex).fill(value)
      }

      Ok(view(preparedForm, departureId, mode, houseConsignmentIndex))
  }

  def onSubmit(departureId: String, mode: Mode, houseConsignmentIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      form(houseConsignmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, houseConsignmentIndex))),
          value => redirect(mode, value, departureId, houseConsignmentIndex)
        )
  }

  private def redirect(
    mode: Mode,
    value: Boolean,
    departureId: String,
    houseConsignmentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), updatedAnswers, departureId, mode))

}
