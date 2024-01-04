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
import forms.SelectableFormProvider
import models.reference.Nationality
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.houseConsignment.index.departureTransportMeans.CountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.NationalitiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.houseConsignment.index.departureTransportMeans.CountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: NationalitiesService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "houseConsignment.index.departureTransportMeans.country"

  def onPageLoad(departureId: String, mode: Mode, houseConsignmentIndex: Index, departureTransportMeansIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        service.getNationalities().map {
          nationalities =>
            val form = formProvider(prefix, nationalities, houseConsignmentIndex.display)
            val preparedForm = request.userAnswers.get(CountryPage(houseConsignmentIndex, departureTransportMeansIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, departureId, nationalities.values, mode, houseConsignmentIndex, departureTransportMeansIndex))
        }
    }

  def onSubmit(departureId: String, mode: Mode, houseConsignmentIndex: Index, departureTransportMeansIndex: Index): Action[AnyContent] =
    actions.requireData(departureId).async {
      implicit request =>
        service.getNationalities().flatMap {
          nationalities =>
            val form = formProvider(prefix, nationalities, houseConsignmentIndex.display)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future
                    .successful(BadRequest(view(formWithErrors, departureId, nationalities.values, mode, houseConsignmentIndex, departureTransportMeansIndex))),
                value => redirect(mode, value, departureId, houseConsignmentIndex, departureTransportMeansIndex)
              )
        }
    }

  private def redirect(
    mode: Mode,
    value: Nationality,
    departureId: String,
    houseConsignmentIndex: Index,
    departureTransportMeansIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryPage(houseConsignmentIndex, departureTransportMeansIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(CountryPage(houseConsignmentIndex, departureTransportMeansIndex), updatedAnswers, departureId, mode))
}
