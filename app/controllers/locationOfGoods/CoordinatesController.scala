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

package controllers.locationOfGoods

import controllers.actions._
import forms.locationOfGoods.CoordinatesFormProvider
import models.requests.MandatoryDataRequest
import models.{Coordinates, Mode}
import navigation.LocationOfGoodsNavigator
import pages.QuestionPage
import pages.locationOfGoods.CoordinatesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.CoordinatesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CoordinatesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  actions: Actions,
  formProvider: CoordinatesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CoordinatesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId) {
      implicit request =>
        val form = formProvider("locationOfGoods.coordinates")
        val preparedForm = request.userAnswers
          .get(CoordinatesPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, departureId, mode))
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val form = formProvider("locationOfGoods.coordinates")
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode))),
            value => redirect(mode, CoordinatesPage, value, departureId)
          )

    }

  private def redirect(
    mode: Mode,
    page: QuestionPage[Coordinates],
    value: Coordinates,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))

}
