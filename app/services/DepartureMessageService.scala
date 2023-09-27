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

import connectors.DepartureMovementConnector
import models.LocalReferenceNumber
import models.departureP5.DepartureMessageMetaData
import models.departureP5.DepartureMessageType._
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMessageService @Inject() (departureMovementP5Connector: DepartureMovementConnector) extends Logging {

  private def getMessageMetaData(departureId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[DepartureMessageMetaData]] =
    departureMovementP5Connector
      .getMessageMetaData(departureId)
      .map(
        _.messages
          .filter(
            message => message.messageType == DepartureNotification || message.messageType == AmendmentSubmitted
          )
          .sortBy(_.received)
          .reverse
          .headOption
      )

  def getLRN(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[LocalReferenceNumber] =
    departureMovementP5Connector.getLRN(departureId)
}
