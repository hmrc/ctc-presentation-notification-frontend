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
import forms.YesNoFormProvider
import models.{Index, Mode}
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.departureTransportMeans.RemoveDepartureTransportMeansYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDepartureTransportMeansYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDepartureTransportMeansYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] =
    formProvider("consignment.departureTransportMeans.removeDepartureTransportMeans")

  private def addAnother(departureId: String, mode: Mode): Call =
    controllers.transport.departureTransportMeans.routes.AddAnotherTransportMeansController.onPageLoad(
      departureId,
      mode
    )

  def onPageLoad(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, TransportMeansSection(transportIndex), addAnother(departureId, mode))
    .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportIndex))) {
      implicit request =>
        val identificationType   = request.arg
        val identificationNumber = request.userAnswers.get(TransportMeansIdentificationNumberPage(transportIndex))
        Ok(view(form, departureId, mode, transportIndex, identificationType, identificationNumber))
    }

  def onSubmit(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, TransportMeansSection(transportIndex), addAnother(departureId, mode))
    .andThen(getMandatoryPage(TransportMeansIdentificationPage(transportIndex)))
    .async {
      implicit request =>
        val identificationType   = request.arg
        val identificationNumber = request.userAnswers.get(TransportMeansIdentificationNumberPage(transportIndex))
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, departureId, mode, transportIndex, identificationType, identificationNumber))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(TransportMeansSection(transportIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(departureId, mode))
          )
    }
}
