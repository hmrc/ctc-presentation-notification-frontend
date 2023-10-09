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

package controllers

import controllers.actions._
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DepartureMessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject() (
  actions: Actions,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  service: DepartureMessageService,
  timeMachine: TimeMachine
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def index(departureId: String): Action[AnyContent] = actions.getData(departureId).async {
    implicit request =>
      service.getDepartureData(departureId).flatMap {
        case Some(departureData) =>
          service.getLRN(departureId) flatMap {
            lrn =>
              sessionRepository
                .set(
                  request.userAnswers.getOrElse(
                    UserAnswers(
                      departureId,
                      request.eoriNumber,
                      lrn.value,
                      JsObject.empty,
                      timeMachine.now(),
                      departureData.data
                    )
                  )
                )
                .map {
                  _ =>
                    if (departureData.data.isDataComplete) {
                      Redirect(controllers.routes.CheckInformationController.onPageLoad(departureId))
                    } else {
                      Redirect(controllers.routes.MoreInformationController.onPageLoad(departureId))
                    }
                }
          }
        case None => Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
      }
  }

}
