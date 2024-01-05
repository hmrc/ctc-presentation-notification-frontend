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
import forms.SelectableFormProvider
import models.{Index, Mode}
import models.reference.Nationality
import models.requests.MandatoryDataRequest
import navigation.BorderNavigator
import pages.transport.transportMeans.TransportMeansNationalityPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.NationalitiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.transportMeans.TransportMeansNationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansNationalityController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: BorderNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: NationalitiesService,
  val controllerComponents: MessagesControllerComponents,
  view: TransportMeansNationalityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().map {
        nationalityList =>
          def nationalityFromDepartureData = {
            val nationalityCode = request.userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
              list => list.headOption.flatMap(_.nationality)
            )

            nationalityCode.flatMap(
              code => nationalityList.values.find(_.code == code)
            )
          }

          val form = formProvider("houseConsignment.index.departureTransportMeans.country", nationalityList, index.display)
          val preparedForm = request.userAnswers.get(TransportMeansNationalityPage(index)).orElse(nationalityFromDepartureData) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, departureId, nationalityList.values, mode, index))
      }
  }

  def onSubmit(departureId: String, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().flatMap {
        nationalityList =>
          val form = formProvider("houseConsignment.index.departureTransportMeans.country", nationalityList, index.display)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, nationalityList.values, mode, index))),
              value => redirect(mode, value, departureId, index)
            )
      }
  }

  private def redirect(
    mode: Mode,
    value: Nationality,
    departureId: String,
    index: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportMeansNationalityPage(index), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(TransportMeansNationalityPage(index), updatedAnswers, departureId, mode))
}
