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
import controllers.actions._
import models.messages.{Data, MessageData}
import models.requests.OptionalDataRequest
import models.{LocalReferenceNumber, UserAnswers}
import play.api.i18n.I18nSupport
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DepartureMessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TimeMachine
import utils.transformer.{IdentificationNumberTransformer, IdentificationTransformer}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject() (
  actions: Actions,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  service: DepartureMessageService,
  identificationTransformer: IdentificationTransformer,
  timeMachine: TimeMachine
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def index(departureId: String): Action[AnyContent] = actions.getData(departureId).async {
    implicit request =>
      (for {
        departureData <- OptionT(service.getDepartureData(departureId))
        lrn           <- OptionT.liftF(retrieveLRN(departureData.data, departureId))
        _ <- OptionT.liftF(
          request.userAnswers match {
            case Some(userAnswers) => sessionRepository.set(userAnswers)
            case None              => generateFromDepartureData(departureId, request, departureData, lrn)
          }
        )
      } yield departureData.data.isDataComplete match {
        case true  => Redirect(controllers.routes.CheckInformationController.onPageLoad(departureId))
        case false => Redirect(controllers.routes.MoreInformationController.onPageLoad(departureId))
      }).getOrElse(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
  }

  private def generateFromDepartureData(departureId: String, request: OptionalDataRequest[AnyContent], departureData: Data, lrn: LocalReferenceNumber)(implicit
    hc: HeaderCarrier
  ): Future[Boolean] = {
    val userAnswers = UserAnswers(departureId, request.eoriNumber, lrn.value, JsObject.empty, timeMachine.now(), departureData.data)
    for {
      withIdentification       <- identificationTransformer.fromDepartureDataToUserAnswers(userAnswers)
      withIdentificationNumber <- Future.fromTry(IdentificationNumberTransformer.fromDepartureDataToUserAnswers(withIdentification))
      result                   <- sessionRepository.set(withIdentificationNumber)
    } yield result
  }

  private def retrieveLRN(messageData: MessageData, departureId: String)(implicit hc: HeaderCarrier): Future[LocalReferenceNumber] =
    messageData.TransitOperation.LRN match {
      case Some(lrn) => Future.successful(LocalReferenceNumber(lrn))
      case _         => service.getLRN(departureId)
    }

}
