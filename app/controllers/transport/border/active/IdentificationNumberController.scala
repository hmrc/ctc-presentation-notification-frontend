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

package controllers.transport.border.active

import controllers.actions._
import forms.border.IdentificationNumberFormProvider
import models.reference.transport.border.active.Identification
import models.{Index, Mode}
import models.requests.{DataRequest, MandatoryDataRequest, SpecificDataRequestProvider1}
import navigation.BorderNavigator
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.MeansOfTransportIdentificationTypesActiveService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.border.active.IdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: BorderNavigator,
  formProvider: IdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationNumberView,
  service: MeansOfTransportIdentificationTypesActiveService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix = "transport.border.active.identificationNumber"

  def onPageLoad(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .async {
        implicit request =>
          val form = formProvider(prefix)

          val fillForm = request.userAnswers
            .get(IdentificationNumberPage(activeIndex))
            .orElse(
              request.userAnswers.departureData.Consignment.ActiveBorderTransportMeans
                .flatMap(
                  seq => seq.lift(activeIndex.position).flatMap(_.identificationNumber)
                )
            )

          val preparedForm = fillForm match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          getIdentification(activeIndex).map {
            case Some(identificationType) => Ok(view(preparedForm, departureId, mode, activeIndex, identificationType.asString))
            case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          }
      }

  def onSubmit(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] =
    actions
      .requireData(departureId)
      .async {
        implicit request =>
          val form = formProvider(prefix)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                getIdentification(activeIndex).map {
                  case Some(identificationType) => BadRequest(view(formWithErrors, departureId, mode, activeIndex, identificationType.asString))
                  case None                     => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
                },
              value => redirect(mode, value, departureId, activeIndex)
            )
      }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String,
    activeIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(IdentificationNumberPage(activeIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(IdentificationNumberPage(activeIndex), updatedAnswers, departureId, mode))

  private def identificationPageIe170(activeIndex: Index)(implicit request: DataRequest[_]): Option[Identification] =
    request.userAnswers.get(IdentificationPage(activeIndex))

  private def identificationPageIe015(activeIndex: Index)(implicit request: DataRequest[_]): Option[String] =
    request.userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
      x => x.lift(activeIndex.position).flatMap(_.typeOfIdentification)
    )

  private def getReferenceDataFor15(activeIndex: Index)(implicit request: DataRequest[_]): Option[Future[Identification]] =
    identificationPageIe015(activeIndex).map {
      str =>
        service.getBorderMeansIdentification(str)
    }

  private def getIdentification(activeIndex: Index)(implicit request: DataRequest[_]): Future[Option[Identification]] = identificationPageIe170(
    activeIndex
  ) match {
    case Some(value) => Future.successful(Some(value))
    case None =>
      getReferenceDataFor15(activeIndex) match {
        case Some(identification) =>
          identification.map(
            Some(_)
          )
        case None => Future.successful(None)
      }
  }

}
