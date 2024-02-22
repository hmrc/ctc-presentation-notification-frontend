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

import base.TestMessageData.{jsonValue, messageData}
import com.github.tomakehurst.wiremock.client.WireMock._
import itbase.{ItSpecBase, WireMockServerHandler}
import models.LocalReferenceNumber
import models.departureP5.DepartureMessageType.{AmendmentSubmitted, DepartureNotification}
import models.departureP5.{DepartureMessageMetaData, DepartureMessageType, DepartureMessages}
import models.messages.Data
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.NodeSeq

class DepartureMovementConnectorSpec extends ItSpecBase with WireMockServerHandler {

  private lazy val connector: DepartureMovementConnector = app.injector.instanceOf[DepartureMovementConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  "DeparturesMovementConnector" - {
    "getLRN" - {

      "must return LocalReferenceNumber" in {

        val responseJson = Json.parse(
          s"""
             |{
             |  "_links": {
             |      "self": {
             |        "href": "/customs/transits/movements/departures/6365135ba5e821ee"
             |      },
             |      "messages": {
             |        "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
             |      }
             |  },
             |  "id": "6365135ba5e821ee",
             |  "movementReferenceNumber": "ABC123",
             |  "localReferenceNumber": "DEF456",
             |  "created": "2022-11-10T15:32:51.459Z",
             |  "updated": "2022-11-10T15:32:51.459Z",
             |  "enrollmentEORINumber": "GB1234567890",
             |  "movementEORINumber": "GB1234567890"
             |}
             |""".stripMargin
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(responseJson.toString()))
        )

        val result = connector.getLRN(departureId).futureValue

        result mustBe LocalReferenceNumber("DEF456")
      }

    }

    "getMessageMetaData" - {

      "must return Messages" in {

        val responseJson: JsValue = Json.parse("""
            |{
            |  "_links": {
            |      "self": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
            |      },
            |      "departure": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
            |      }
            |  },
            |  "messages": [
            |    {
            |      "_links": {
            |        "self": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            |        },
            |        "departure": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
            |        }
            |      },
            |      "id": "634982098f02f00a",
            |      "departureId": "6365135ba5e821ee",
            |      "received": "2022-11-11T15:32:51.459Z",
            |      "type": "IE015",
            |      "status": "Success"
            |    },
            |    {
            |      "_links": {
            |        "self": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/message/634982098f02f00a"
            |        },
            |        "departure": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
            |        }
            |      },
            |      "id": "634982098f02f00a",
            |      "departureId": "6365135ba5e821ee",
            |      "received": "2022-11-10T15:32:51.459Z",
            |      "type": "IE013",
            |      "status": "Success"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val expectedResult = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            ),
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AmendmentSubmitted,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getMessageMetaData(departureId).futureValue mustBe expectedResult

      }
    }

    "getData" - {
      "when IE015 messageData" in {
        val jsonIE015 = Json.parse(s"""
             |{
             |  "type": "IE015",
             |  "body" : {
             |    "n1:CC015C": $jsonValue
             |  }
             |}
             |""".stripMargin)

        server.stubFor(
          get(urlEqualTo(s"/$departureId"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(jsonIE015.toString()))
        )

        val result: Data = connector.getData(departureId, DepartureNotification).futureValue

        val expectedResult =
          Data(
            messageData
          )

        result mustBe expectedResult
      }

      "when IE013 messageData" in {
        val jsonIE013 = Json.parse(s"""
             |{
             |  "type": "IE013",
             |  "body" : {
             |    "n1:CC013C": $jsonValue
             |  }
             |}
             |""".stripMargin)

        server.stubFor(
          get(urlEqualTo(s"/$departureId"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(jsonIE013.toString()))
        )

        val result: Data = connector.getData(departureId, AmendmentSubmitted).futureValue

        val expectedResult =
          Data(
            messageData
          )

        result mustBe expectedResult
      }
    }

    "submit" - {
      val body: NodeSeq =
        <ncts:CC170C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
          <messageSender>token</messageSender>
        </ncts:CC170C>

      "must return OK for successful response" in {
        server.stubFor(
          post(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .withRequestBody(equalTo(body.toString()))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .withHeader("Content-Type", equalTo("application/xml"))
            .willReturn(aResponse().withStatus(OK))
        )

        val result: HttpResponse = connector.submit(body, departureId).futureValue

        result.status mustBe OK
      }
    }
  }
}
