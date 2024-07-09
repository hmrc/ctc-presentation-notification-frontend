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

package controllers.houseConsignment.index.departureTransportMeans

import controllers.actions._
import forms.EnumerableFormProvider
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.QuestionPage
import pages.houseConsignment.index.departureTransportMeans.IdentificationPage
import pages.transport.InlandModePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransportMeansIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.departureTransportMeans.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView,
  service: TransportMeansIdentificationTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(identificationTypes: Seq[TransportMeansIdentification], houseConsignmentIndex: Index): Form[TransportMeansIdentification] =
    formProvider[TransportMeansIdentification]("houseConsignment.index.departureTransportMeans.identification",
                                               identificationTypes,
                                               houseConsignmentIndex.display
    )

  def onPageLoad(departureId: String, mode: Mode, houseConsignmentIndex: Index, departureTransportMeansIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(departureTransportMeansIndex, request.userAnswers.get(InlandModePage)).flatMap {
          identifiers =>
            val preparedForm = request.userAnswers.get(IdentificationPage(houseConsignmentIndex, departureTransportMeansIndex)) match {
              case None        => form(identifiers, houseConsignmentIndex)
              case Some(value) => form(identifiers, houseConsignmentIndex).fill(value)
            }

            Future.successful(Ok(view(preparedForm, departureId, identifiers, mode, houseConsignmentIndex, departureTransportMeansIndex)))
        }
    }

  def onSubmit(departureId: String, mode: Mode, houseConsignmentIndex: Index, departureTransportMeansIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getMeansOfTransportIdentificationTypes(departureTransportMeansIndex, request.userAnswers.get(InlandModePage)).flatMap {
          identificationTypeList =>
            form(identificationTypeList, houseConsignmentIndex)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(view(formWithErrors, departureId, identificationTypeList, mode, houseConsignmentIndex, departureTransportMeansIndex))
                  ),
                value => redirect(mode, IdentificationPage, value, departureId, houseConsignmentIndex, departureTransportMeansIndex)
              )
        }
    }

  private def redirect(
    mode: Mode,
    page: (Index, Index) => QuestionPage[TransportMeansIdentification],
    value: TransportMeansIdentification,
    departureId: String,
    houseConsignmentIndex: Index,
    departureTransportMeansIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page(houseConsignmentIndex, departureTransportMeansIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page(houseConsignmentIndex, departureTransportMeansIndex), updatedAnswers, departureId, mode))
}
