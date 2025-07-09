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
import models.departureP5.*
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.ws.XMLBodyWritables.*
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsTry, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

class DepartureMovementConnector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsTry
    with Logging {

  private val version = config.isPhase6Enabled match {
    case _ => 2.1
  }

  private val jsonHeader: (String, String) =
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.$version+json"

  private val xmlHeader: (String, String) =
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.$version+xml"

  def getMessages(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureMessages] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"
    http
      .get(url)
      .setHeader(jsonHeader)
      .execute[DepartureMessages]
  }

  def getLRN(departureId: String)(implicit hc: HeaderCarrier): Future[LocalReferenceNumber] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId"
    http
      .get(url)
      .setHeader(jsonHeader)
      .execute[LocalReferenceNumber]
  }

  def submit(xml: NodeSeq, departureId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"
    http
      .post(url)
      .setHeader(jsonHeader)
      .setHeader(CONTENT_TYPE -> "application/xml")
      .withBody(xml)
      .execute[HttpResponse]
  }

  def getMessage[T](departureId: String, messageId: String)(implicit hc: HeaderCarrier, format: XMLFormat[T]): Future[T] = {
    val url = url"${config.commonTransitConventionTradersUrl}/movements/departures/$departureId/messages/$messageId/body"
    http
      .get(url)
      .setHeader(xmlHeader)
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
      .map(fromXML(_))
  }
}
