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
  implicit val sessionRepository: SessionRepository,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  navigator: DepartureTransportMeansNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "consignment.departureTransportMeans.identificationNumber"

  def onPageLoad(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId) {
        implicit request =>
          val form = formProvider(prefix)

          val fillForm = request.userAnswers
            .get(TransportMeansIdentificationNumberPage(transportIndex))

          val preparedForm = fillForm match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          request.userAnswers.get(TransportMeansIdentificationPage(transportIndex)) match {
            case Some(identificationType) => Ok(view(preparedForm, departureId, mode, identificationType.asString, transportIndex))
            case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          }
      }

  def onSubmit(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .async {
        implicit request =>
          val form = formProvider(prefix)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(request.userAnswers.get(TransportMeansIdentificationPage(transportIndex)) match {
                  case Some(identificationType) => BadRequest(view(formWithErrors, departureId, mode, identificationType.asString, transportIndex))
                  case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
                }),
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
