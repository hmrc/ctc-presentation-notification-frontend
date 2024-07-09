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

import controllers.actions._
import forms.border.IdentificationNumberFormProvider
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.DepartureTransportMeansNavigator
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.departureTransportMeans.TransportMeansIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  navigator: DepartureTransportMeansNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "consignment.departureTransportMeans.identificationNumber"

  private val form = formProvider(prefix)

  def onPageLoad(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportIndex))) {
        implicit request =>
          val preparedForm = request.userAnswers.get(TransportMeansIdentificationNumberPage(transportIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, departureId, mode, request.arg.asString, transportIndex))
      }

  def onSubmit(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportIndex)))
      .async {
        implicit request =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, request.arg.asString, transportIndex))),
              value => redirect(value, departureId, mode, transportIndex)
            )
      }

  private def redirect(
    value: String,
    departureId: String,
    mode: Mode,
    transportIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationNumberPage(transportIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationNumberPage(transportIndex), updatedAnswers, departureId, mode))
}
