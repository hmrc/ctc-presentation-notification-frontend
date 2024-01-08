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

package controllers.transport.transportMeans

import controllers.actions._
import forms.EnumerableFormProvider
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.QuestionPage
import pages.transport.transportMeans.TransportMeansIdentificationPage
import pages.transport.InlandModePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{MeansOfTransportIdentificationTypesService, TransportModeCodesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.transportMeans.TransportMeansIdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansIdentificationView,
  service: MeansOfTransportIdentificationTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[TransportMeansIdentification]): Form[TransportMeansIdentification] =
    formProvider[TransportMeansIdentification]("houseConsignment.index.departureTransportMeans.identification", identificationTypes)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(request.userAnswers.get(InlandModePage)).flatMap {
          identifiers =>
            val preparedForm = request.userAnswers.get(TransportMeansIdentificationPage) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }
            Future.successful(Ok(view(preparedForm, departureId, identifiers, mode)))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
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
                    BadRequest(view(formWithErrors, departureId, identificationTypeList, mode))
                  ),
                value => redirect(mode, value, departureId)
              )
        }
    }

  private def redirect(
    mode: Mode,
    value: TransportMeansIdentification,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationPage, updatedAnswers, departureId, mode))
}
