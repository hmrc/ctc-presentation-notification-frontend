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

import cats.data.OptionT
import controllers.actions.*
import generated.CC015CType
import models.requests.OptionalDataRequest
import models.{LocalReferenceNumber, RichCC015CType, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{DateTimeService, DepartureMessageService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.transformer.DepartureDataTransformer

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject() (
  actions: Actions,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  service: DepartureMessageService,
  departureDataTransformer: DepartureDataTransformer,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def redirect(departureId: String): Action[AnyContent] = actions.getData(departureId).async {
    implicit request =>
      logger.info(s"Entering IE170 journey for departure ID $departureId")
      (
        for {
          lrn <- OptionT.liftF(service.getLRN(departureId))
          _ = logger.info(s"LRN $lrn found for departure ID $departureId")
          departureData <- OptionT(service.getDepartureData(departureId, lrn))
          _                         = logger.info(s"Departure data found for departure ID $departureId")
          additionalDeclarationType = departureData.TransitOperation.additionalDeclarationType
          canSubmitPresentationNotification <- OptionT.liftF(service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType))
          if canSubmitPresentationNotification
          _ <- OptionT.liftF(
            request.userAnswers match {
              case Some(userAnswers) =>
                logger.info(s"User answers found for departure ID $departureId")
                sessionRepository.set(userAnswers)
              case None =>
                logger.info(s"No user answers found for departure ID $departureId")
                generateFromDepartureData(departureId, lrn, departureData)
            }
          )
        } yield
          if (departureData.isDataComplete) {
            Redirect(controllers.routes.CheckInformationController.onPageLoad(departureId))
          } else {
            Redirect(controllers.routes.MoreInformationController.onPageLoad(departureId))
          }
      ).getOrElse(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
  }

  private def generateFromDepartureData(
    departureId: String,
    lrn: LocalReferenceNumber,
    departureData: CC015CType
  )(implicit request: OptionalDataRequest[?]): Future[Boolean] = {
    val userAnswers = UserAnswers(departureId, request.eoriNumber, lrn.value, JsObject.empty, dateTimeService.currentInstant, departureData)
    for {
      updatedUserAnswers <- departureDataTransformer.transform(userAnswers)
      _ = logger.info(s"Transformation complete for departure ID $departureId")
      result <- sessionRepository.set(updatedUserAnswers)
    } yield result
  }
}
