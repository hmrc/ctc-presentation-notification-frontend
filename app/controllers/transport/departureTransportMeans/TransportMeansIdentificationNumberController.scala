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
import models.Mode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.{DataRequest, MandatoryDataRequest}
import navigation.Navigator
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.departureTransportMeans.TransportMeansIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansIdentificationNumberView,
  service: MeansOfTransportIdentificationTypesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "consignment.departureTransportMeans.identificationNumber"

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] =
    actions
      .requireData(departureId)
      .async {
        implicit request =>
          val form = formProvider(prefix)

          val fillForm = request.userAnswers
            .get(TransportMeansIdentificationNumberPage)
            .orElse(
              request.userAnswers.departureData.Consignment.DepartureTransportMeans
                .flatMap(
                  _.identificationNumber
                )
            )

          val preparedForm = fillForm match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          getIdentification().map {
            case Some(identificationType) => Ok(view(preparedForm, departureId, mode, identificationType.asString))
            case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          }
      }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] =
    actions
      .requireData(departureId)
      .async {
        implicit request =>
          val form = formProvider(prefix)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                getIdentification().map {
                  case Some(identificationType) => BadRequest(view(formWithErrors, departureId, mode, identificationType.asString))
                  case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
                },
              value => redirect(mode, value, departureId)
            )
      }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansIdentificationNumberPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansIdentificationNumberPage, updatedAnswers, departureId, mode))

  private def identificationPageIe170(implicit request: DataRequest[_]): Option[TransportMeansIdentification] =
    request.userAnswers.get(TransportMeansIdentificationPage)

  private def identificationPageIe015(implicit request: DataRequest[_]): Option[String] =
    request.userAnswers.departureData.Consignment.DepartureTransportMeans.flatMap(
      _.typeOfIdentification
    )

  private def getReferenceDataFor15(implicit request: DataRequest[_]): Option[Future[TransportMeansIdentification]] =
    identificationPageIe015.map {
      str =>
        service.getBorderMeansIdentification(str)
    }

  private def getIdentification()(implicit request: DataRequest[_]): Future[Option[TransportMeansIdentification]] = identificationPageIe170 match {
    case Some(value) => Future.successful(Some(value))
    case None =>
      getReferenceDataFor15 match {
        case Some(identification) =>
          identification.map(
            Some(_)
          )
        case None => Future.successful(None)
      }
  }

}
