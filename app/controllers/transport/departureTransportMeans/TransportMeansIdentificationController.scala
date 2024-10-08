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
import forms.EnumerableFormProvider
import models.{Index, Mode}
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import navigation.DepartureTransportMeansNavigator
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans.TransportMeansIdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.departureTransportMeans.TransportMeansIdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansIdentificationView,
  navigator: DepartureTransportMeansNavigator,
  service: MeansOfTransportIdentificationTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[TransportMeansIdentification]): Form[TransportMeansIdentification] =
    formProvider[TransportMeansIdentification]("consignment.departureTransportMeans.identification", identificationTypes)

  def onPageLoad(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers.get(InlandModePage)).flatMap {
          identifiers =>
            val preparedForm = request.userAnswers.get(TransportMeansIdentificationPage(transportIndex)) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }

            Future.successful(Ok(view(preparedForm, departureId, identifiers, mode, transportIndex)))
        }
    }

  def onSubmit(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers.get(InlandModePage)).flatMap {
          identificationTypeList =>
            form(identificationTypeList)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(view(formWithErrors, departureId, identificationTypeList, mode, transportIndex))
                  ),
                value => redirect(value, departureId, mode, transportIndex)
              )
        }
    }

  private def redirect(
    value: TransportMeansIdentification,
    departureId: String,
    mode: Mode,
    transportIndex: Index
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationPage(transportIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationPage(transportIndex), updatedAnswers, departureId, mode))
}
