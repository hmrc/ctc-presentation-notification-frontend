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

import com.github.tomakehurst.wiremock.client.WireMock._
import generated._
import itbase.{ItSpecBase, WireMockServerHandler}
import models.LocalReferenceNumber
import models.departureP5.{DepartureMessages, MessageMetaData, MessageType}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.OK
import scalaxb.XMLCalendar
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.{Node, NodeSeq}

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

    "getMessages" - {

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
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages/634982098f02f00b"
            |        },
            |        "departure": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee"
            |        }
            |      },
            |      "id": "634982098f02f00b",
            |      "departureId": "6365135ba5e821ee",
            |      "received": "2022-11-11T15:32:51.459Z",
            |      "type": "IE015",
            |      "status": "Success"
            |    },
            |    {
            |      "_links": {
            |        "self": {
            |          "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages/634982098f02f00a"
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
            MessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              MessageType.DepartureNotification,
              "634982098f02f00b"
            ),
            MessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              MessageType.AmendmentSubmitted,
              "634982098f02f00a"
            )
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getMessages(departureId).futureValue mustBe expectedResult
      }
    }

    "getMessage" - {

      val messageId = "messageId"

      "when IE015 messageData" in {
        val xml: Node =
          <ncts:CC015C xmlns:ncts="http://ncts.dgtaxud.ec">
            <messageSender>message sender</messageSender>
            <messageRecipient>NTA.GB</messageRecipient>
            <preparationDateAndTime>2022-01-22T07:43:36</preparationDateAndTime>
            <messageIdentification>messageId</messageIdentification>
            <messageType>CC015C</messageType>
            <TransitOperation>
              <LRN>HnVr</LRN>
              <declarationType>Pbg</declarationType>
              <additionalDeclarationType>A</additionalDeclarationType>
              <security>1</security>
              <reducedDatasetIndicator>1</reducedDatasetIndicator>
              <bindingItinerary>0</bindingItinerary>
            </TransitOperation>
            <CustomsOfficeOfDeparture>
              <referenceNumber>GB000060</referenceNumber>
            </CustomsOfficeOfDeparture>
            <CustomsOfficeOfDestinationDeclared>
              <referenceNumber>XI000142</referenceNumber>
            </CustomsOfficeOfDestinationDeclared>
            <HolderOfTheTransitProcedure>
              <identificationNumber>idNumber</identificationNumber>
            </HolderOfTheTransitProcedure>
            <Consignment>
              <grossMass>6430669292.48125</grossMass>
            </Consignment>
          </ncts:CC015C>

        val expectedResult = CC015CType(
          messageSequence1 = MESSAGESequence(
            messageSender = "message sender",
            messagE_1Sequence2 = MESSAGE_1Sequence(
              messageRecipient = "NTA.GB",
              preparationDateAndTime = XMLCalendar("2022-01-22T07:43:36"),
              messageIdentification = "messageId"
            ),
            messagE_TYPESequence3 = MESSAGE_TYPESequence(
              messageType = CC015C
            ),
            correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence()
          ),
          TransitOperation = TransitOperationType06(
            LRN = "HnVr",
            declarationType = "Pbg",
            additionalDeclarationType = "A",
            security = "1",
            reducedDatasetIndicator = Number1,
            bindingItinerary = Number0
          ),
          CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
            referenceNumber = "GB000060"
          ),
          CustomsOfficeOfDestinationDeclared = CustomsOfficeOfDestinationDeclaredType01(
            referenceNumber = "XI000142"
          ),
          HolderOfTheTransitProcedure = HolderOfTheTransitProcedureType14(
            identificationNumber = Some("idNumber")
          ),
          Consignment = ConsignmentType20(
            grossMass = 6430669292.48125
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages/$messageId/body"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+xml"))
            .willReturn(ok(xml.toString()))
        )

        val result = connector.getMessage[CC015CType](departureId, messageId).futureValue

        result mustBe expectedResult
      }

      "when IE013 messageData" in {
        val xml: Node =
          <ncts:CC013C xmlns:ncts="http://ncts.dgtaxud.ec">
            <messageSender>message sender</messageSender>
            <messageRecipient>NTA.GB</messageRecipient>
            <preparationDateAndTime>2022-01-22T07:43:36</preparationDateAndTime>
            <messageIdentification>messageId</messageIdentification>
            <messageType>CC015C</messageType>
            <TransitOperation>
              <LRN>HnVr</LRN>
              <declarationType>Pbg</declarationType>
              <additionalDeclarationType>A</additionalDeclarationType>
              <security>1</security>
              <reducedDatasetIndicator>1</reducedDatasetIndicator>
              <bindingItinerary>0</bindingItinerary>
              <amendmentTypeFlag>0</amendmentTypeFlag>
            </TransitOperation>
            <CustomsOfficeOfDeparture>
              <referenceNumber>GB000060</referenceNumber>
            </CustomsOfficeOfDeparture>
            <CustomsOfficeOfDestinationDeclared>
              <referenceNumber>XI000142</referenceNumber>
            </CustomsOfficeOfDestinationDeclared>
            <HolderOfTheTransitProcedure>
              <identificationNumber>idNumber</identificationNumber>
            </HolderOfTheTransitProcedure>
            <Consignment>
              <grossMass>6430669292.48125</grossMass>
            </Consignment>
          </ncts:CC013C>

        val expectedResult = CC013CType(
          messageSequence1 = MESSAGESequence(
            messageSender = "message sender",
            messagE_1Sequence2 = MESSAGE_1Sequence(
              messageRecipient = "NTA.GB",
              preparationDateAndTime = XMLCalendar("2022-01-22T07:43:36"),
              messageIdentification = "messageId"
            ),
            messagE_TYPESequence3 = MESSAGE_TYPESequence(
              messageType = CC015C
            ),
            correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence()
          ),
          TransitOperation = TransitOperationType04(
            LRN = Some("HnVr"),
            declarationType = "Pbg",
            additionalDeclarationType = "A",
            security = "1",
            reducedDatasetIndicator = Number1,
            bindingItinerary = Number0,
            amendmentTypeFlag = Number0
          ),
          CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
            referenceNumber = "GB000060"
          ),
          CustomsOfficeOfDestinationDeclared = CustomsOfficeOfDestinationDeclaredType01(
            referenceNumber = "XI000142"
          ),
          HolderOfTheTransitProcedure = HolderOfTheTransitProcedureType14(
            identificationNumber = Some("idNumber")
          ),
          Consignment = ConsignmentType20(
            grossMass = 6430669292.48125
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages/$messageId/body"))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+xml"))
            .willReturn(ok(xml.toString()))
        )

        val result = connector.getMessage[CC013CType](departureId, messageId).futureValue

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
