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
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.QuestionPage
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.transportMeans.TransportMeansIdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransportMeansIdentificationTypesService
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
  service: TransportMeansIdentificationTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[TransportMeansIdentification]): Form[TransportMeansIdentification] =
    formProvider[TransportMeansIdentification]("transport.transportMeans.identification", identificationTypes)

  def onPageLoad(departureId: String, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(index, request.userAnswers.get(BorderModeOfTransportPage)).flatMap {
          identifiers =>
            def identificationFromDepartureData = {
              val identificationCode = request.userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
                list => list.lift(index.position).flatMap(_.typeOfIdentification)
              )
              identificationCode.flatMap(
                code => identifiers.find(_.code == code)
              )
            }

            val preparedForm = request.userAnswers.get(TransportMeansIdentificationPage(index)).orElse(identificationFromDepartureData) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }

            Future.successful(Ok(view(preparedForm, departureId, identifiers, mode, index)))
        }
    }

  def onSubmit(departureId: String, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(index, request.userAnswers.get(BorderModeOfTransportPage)).flatMap {
          identificationTypeList =>
            form(identificationTypeList)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(view(formWithErrors, departureId, identificationTypeList, mode, index))
                  ),
                value => redirect(mode, TransportMeansIdentificationPage, value, departureId, index)
              )
        }
    }

  private def redirect(
    mode: Mode,
    page: Index => QuestionPage[TransportMeansIdentification],
    value: TransportMeansIdentification,
    departureId: String,
    index: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page(index), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page(index), updatedAnswers, departureId, mode))
}
