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

package controllers.transportMeans

import controllers.actions._
import forms.SelectableFormProvider
import models.reference.Nationality
import models.requests.MandatoryDataRequest
import models.{Index, Mode}
import navigation.TransportMeansNavigator
import pages.transportMeans.NationalityPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.NationalitiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transportMeans.NationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NationalityController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: TransportMeansNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  service: NationalitiesService,
  val controllerComponents: MessagesControllerComponents,
  view: NationalityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().map {
        nationalityList =>
          val form = formProvider("transportMeans.nationality", nationalityList)
          val preparedForm = request.userAnswers.get(NationalityPage(activeIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, departureId, nationalityList.values, mode, activeIndex))
      }
  }

  def onSubmit(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      service.getNationalities().flatMap {
        nationalityList =>
          val form = formProvider("transportMeans.nationality", nationalityList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, nationalityList.values, mode, activeIndex))),
              value => redirect(mode, value, departureId, activeIndex)
            )
      }
  }

  private def redirect(
    mode: Mode,
    value: Nationality,
    departureId: String,
    activeIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(NationalityPage(activeIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(NationalityPage(activeIndex), updatedAnswers, departureId, mode))

}
