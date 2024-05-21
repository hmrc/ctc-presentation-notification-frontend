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

package services

import cats.data.OptionT
import connectors.DepartureMovementConnector
import generated.{CC013CType, CC015CType, CC170CType}
import models.departureP5.MessageType.{DeclarationAmendment, DeclarationData, PresentationForThePreLodgedDeclaration}
import models.departureP5.{MessageMetaData, MessageType}
import models.{LocalReferenceNumber, RichCC013CType}
import play.api.Logging
import scalaxb.XMLFormat
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMessageService @Inject() (departureMovementP5Connector: DepartureMovementConnector) extends Logging {

  private def getMessageMetaData(
    departureId: String,
    messageTypes: MessageType*
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    departureMovementP5Connector
      .getMessages(departureId)
      .map(
        _.messages
          .filter {
            message => messageTypes.contains(message.messageType)
          }
          .sortBy(_.received)
          .reverse
          .headOption
      )

  def getIE170(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CC170CType]] =
    (for {
      messageMetaData <- OptionT(getMessageMetaData(departureId, PresentationForThePreLodgedDeclaration))
      message         <- OptionT.liftF(getMessage[CC170CType](departureId, messageMetaData.id))
    } yield message).value

  def getDepartureData(
    departureId: String,
    lrn: LocalReferenceNumber
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[CC015CType]] =
    (for {
      messageMetaData <- OptionT(getMessageMetaData(departureId, DeclarationData, DeclarationAmendment))
      message <- messageMetaData.messageType match {
        case DeclarationData =>
          OptionT.liftF(getMessage[CC015CType](departureId, messageMetaData.id))
        case DeclarationAmendment =>
          OptionT.liftF(getMessage[CC013CType](departureId, messageMetaData.id).map(_.toCC015CType(lrn)))
        case _ =>
          OptionT[Future, CC015CType](Future.successful(None))
      }
    } yield message).value

  def getLRN(departureId: String)(implicit hc: HeaderCarrier): Future[LocalReferenceNumber] =
    departureMovementP5Connector.getLRN(departureId)

  private def getMessage[T](departureId: String, messageId: String)(implicit hc: HeaderCarrier, format: XMLFormat[T]): Future[T] =
    departureMovementP5Connector.getMessage(departureId, messageId)
}
