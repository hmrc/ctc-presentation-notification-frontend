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

import controllers.actions.*
import forms.SelectableFormProvider.CountryFormProvider
import models.reference.Nationality
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.DepartureTransportMeansNavigator
import pages.transport.departureTransportMeans.TransportMeansNationalityPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.NationalitiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.departureTransportMeans.TransportMeansNationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansNationalityController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: DepartureTransportMeansNavigator,
  actions: Actions,
  formProvider: CountryFormProvider,
  service: NationalitiesService,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansNationalityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().map {
        nationalityList =>
          val form = formProvider("consignment.departureTransportMeans.nationality", nationalityList)
          val preparedForm = request.userAnswers.get(TransportMeansNationalityPage(transportIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, departureId, nationalityList.values, mode, transportIndex))
      }
  }

  def onSubmit(departureId: String, mode: Mode, transportIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().flatMap {
        nationalityList =>
          val form = formProvider("consignment.departureTransportMeans.nationality", nationalityList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, nationalityList.values, mode, transportIndex))),
              value => redirect(value, departureId, mode, transportIndex)
            )
      }
  }

  private def redirect(
    value: Nationality,
    departureId: String,
    mode: Mode,
    transportIndex: Index
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansNationalityPage(transportIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansNationalityPage(transportIndex), updatedAnswers, departureId, mode))
}
