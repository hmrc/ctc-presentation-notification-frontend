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

package connectors

import config.FrontendAppConfig
import models.LocalReferenceNumber
import models.departureP5._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsTry}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMovementConnector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends HttpReadsTry {

  private def headers(implicit hc: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

  def getMessageMetaData(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureMessages] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"
    http.GET[DepartureMessages](url)(implicitly, headers, ec)
  }

  def getLRN(departureId: String)(implicit hc: HeaderCarrier): Future[LocalReferenceNumber] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId"
    http.GET[LocalReferenceNumber](url)(HttpReads[LocalReferenceNumber], headers, ec)
  }
}
