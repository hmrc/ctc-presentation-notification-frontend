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

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import itbase.{ItSpecBase, WireMockServerHandler}
import models.reference.transport.border.active.Identification
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.*
import models.LocationOfGoodsIdentification
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customs-reference-data.port" -> server.port()
    )

  private lazy val asyncCacheApi: AsyncCacheApi = app.injector.instanceOf[AsyncCacheApi]

  private lazy val phase5App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> false)

  private lazy val phase6App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> true)

  override def beforeEach(): Unit = {
    super.beforeEach()
    asyncCacheApi.removeAll().futureValue
  }

  private val customsOfficesResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/CustomsOffices"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "CustomsOffices",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "id": "GB1",
      |      "name": "testName1",
      |      "languageCode": "EN",
      |      "countryId": "GB",
      |      "eMailAddress": "foo@andorra.ad",
      |      "roles": [
      |        {
      |          "role": "DEP"
      |        }
      |      ]
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "id": "GB2",
      |      "name": "testName2",
      |      "languageCode": "ES",
      |      "countryId": "GB",
      |      "roles": [
      |        {
      |          "role": "DEP"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

  private def countriesResponseJson(listName: String): String =
    s"""
       |{
       |  "_links": {
       |    "self": {
       |      "href": "/customs-reference-data/lists/$listName"
       |    }
       |  },
       |  "meta": {
       |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
       |    "snapshotDate": "2023-01-01"
       |  },
       |  "id": "$listName",
       |  "data": [
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "GB",
       |      "state": "valid",
       |      "description": "United Kingdom"
       |    },
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "AD",
       |      "state": "valid",
       |      "description": "Andorra"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val countriesResponseP6Json: String =
    s"""
       |[
       |  {
       |    "key": "GB",
       |    "value": "United Kingdom"
       |  },
       |  {
       |    "key": "AD",
       |    "value": "Andorra"
       |  }
       |]
       |""".stripMargin

  private val countryResponseJson: String =
    s"""
       |{
       |  "_links": {
       |    "self": {
       |      "href": "/customs-reference-data/lists/CountryWithoutZip"
       |    }
       |  },
       |  "meta": {
       |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
       |    "snapshotDate": "2023-01-01"
       |  },
       |  "id": "CountryWithoutZip",
       |  "data": [
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "GB",
       |      "state": "valid",
       |      "description": "United Kingdom"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val countryResponseP6Json: String =
    s"""
       |[
       |    {
       |      "key": "GB",
       |      "value": "United Kingdom"
       |    }
       |]
       |""".stripMargin

  private val unLocodesResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/UnLocodeExtended"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "UnLocodeExtended",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "unLocodeExtendedCode": "UN1",
      |      "name": "testName1"
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "unLocodeExtendedCode": "UN2",
      |      "name": "testName2"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val unLocodesResponseP6Json: String =
    """
      |[
      |    {
      |      "key": "UN1",
      |      "value": "testName1"
      |    },
      |    {
      |      "key": "UN2",
      |      "value": "testName2"
      |    }
      |]
      |""".stripMargin

  private val unLocodeResponseJson: String = """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/UnLocodeExtended"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "UnLocodeExtended",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "unLocodeExtendedCode": "UN1",
      |      "name": "testName1"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val unLocodeResponseP6Json: String = """
      |[
      |    {
      |      "key": "UN1",
      |      "value": "testName1"
      |    }
      |]
      |""".stripMargin

  private val nationalitiesResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/Nationality"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "Nationality",
      |  "data": [
      |    {
      |      "code":"AR",
      |      "description":"Argentina"
      |    },
      |    {
      |      "code":"AU",
      |      "description":"Australia"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val nationalitiesResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"AR",
      |      "value":"Argentina"
      |    },
      |    {
      |      "key":"AU",
      |      "value":"Australia"
      |    }
      |]
      |""".stripMargin

  private val nationalityResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/Nationality"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "Nationality",
      |  "data": [
      |    {
      |      "code":"AR",
      |      "description":"Argentina"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val nationalityResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"AR",
      |      "value":"Argentina"
      |    }
      |]
      |""".stripMargin

  private val specificCircumstanceIndicatorsResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/SpecificCircumstanceIndicatorCode"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "SpecificCircumstanceIndicatorCode",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "code": "SCI1",
      |      "description": "testName1"
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "code": "SCI2",
      |      "description": "testName2"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val specificCircumstanceIndicatorsResponseP6Json: String =
    """
      |[
      |    {
      |      "key": "SCI1",
      |      "value": "testName1"
      |    },
      |    {
      |      "key": "SCI2",
      |      "value": "testName2"
      |    }
      |]
      |""".stripMargin

  private val locationTypesResponseJson: String =
    """
      |{
      |   "data": [
      |              {
      |                "type": "A",
      |                "description": "Designated location"
      |              },
      |              {
      |                "type": "B",
      |                "description": "Authorised place"
      |               }
      |            ]
      |}
      |""".stripMargin

  private val locationTypesResponseP6Json: String =
    """
      |[
      |  {
      |    "key": "A",
      |    "value": "Designated location"
      |  },
      |  {
      |    "key": "B",
      |    "value": "Authorised place"
      |   }
      |]
      |""".stripMargin

  private val locationTypeResponseJson: String =
    """
      |{
      |   "data": [
      |              {
      |                "type": "A",
      |                "description": "Designated location"
      |              }
      |            ]
      |}
      |""".stripMargin

  private val locationTypeResponseP6Json: String =
    """
      |[
      |  {
      |    "key": "A",
      |    "value": "Designated location"
      |  }
      |]
      |""".stripMargin

  private val locationOfGoodsIdentificationResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/QualifierOfTheIdentification"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "QualifierOfTheIdentification",
      |  "data": [
      |    {
      |      "qualifier":"T",
      |      "description":"Postal code"
      |    },
      |    {
      |      "qualifier":"X",
      |      "description":"EORI number"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val locationOfGoodsIdentificationResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"T",
      |      "value":"Postal code"
      |    },
      |    {
      |      "key":"X",
      |      "value":"EORI number"
      |    }
      |]
      |""".stripMargin

  private val meansOfTransportIdentificationTypesActiveResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/TypeOfIdentificationofMeansOfTransportActive"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "TypeOfIdentificationofMeansOfTransportActive",
      |  "data": [
      |    {
      |      "code":"10",
      |      "description":"IMO Ship Identification Number"
      |    },
      |    {
      |      "code":"11",
      |      "description":"Name of the sea-going vessel"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val meansOfTransportIdentificationTypesActiveResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"10",
      |      "value":"IMO Ship Identification Number"
      |    },
      |    {
      |      "key":"11",
      |      "value":"Name of the sea-going vessel"
      |    }
      |]
      |""".stripMargin

  private val meansOfTransportIdentificationTypeActiveResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/TypeOfIdentificationofMeansOfTransportActive"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "TypeOfIdentificationofMeansOfTransportActive",
      |  "data": [
      |    {
      |      "code":"10",
      |      "description":"IMO Ship Identification Number"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val meansOfTransportIdentificationTypeActiveResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"10",
      |      "value":"IMO Ship Identification Number"
      |    }
      |]
      |""".stripMargin

  private val meansOfTransportIdentificationTypesResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/TypeOfIdentificationOfMeansOfTransport"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "TypeOfIdentificationOfMeansOfTransport",
      |  "data": [
      |    {
      |      "type":"10",
      |      "description":"IMO Ship Identification Number"
      |    },
      |    {
      |      "type":"11",
      |      "description":"Name of the sea-going vessel"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val meansOfTransportIdentificationTypesResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"10",
      |      "value":"IMO Ship Identification Number"
      |    },
      |    {
      |      "key":"11",
      |      "value":"Name of the sea-going vessel"
      |    }
      |]
      |""".stripMargin

  private val meansOfTransportIdentificationTypeResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/TypeOfIdentificationofMeansOfTransportActive"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "TypeOfIdentificationofMeansOfTransportActive",
      |  "data": [
      |    {
      |      "type":"10",
      |      "description":"IMO Ship Identification Number"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val meansOfTransportIdentificationTypeResponseP6Json: String =
    """
      |[
      |    {
      |      "key":"10",
      |      "value":"IMO Ship Identification Number"
      |    }
      |]
      |""".stripMargin

  private val emptyPhase5ResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  private val emptyPhase6ResponseJson: String =
    """
      |[]
      |""".stripMargin

  "Reference Data" - {

    "getTypesOfLocation" - {
      val url = s"/$baseUrl/lists/TypeOfLocation"

      "when phase 5" - {
        "must return Seq of security types when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(locationTypesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                LocationType("A", "Designated location"),
                LocationType("B", "Authorised place")
              )

              connector.getTypesOfLocation().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getTypesOfLocation())
          }
        }

        "must handle client and server errors for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTypesOfLocation())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of security types when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(locationTypesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                LocationType("A", "Designated location"),
                LocationType("B", "Authorised place")
              )

              connector.getTypesOfLocation().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getTypesOfLocation())
          }
        }

        "must handle client and server errors for control types" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getTypesOfLocation())
          }
        }
      }
    }

    "getTypeOfLocation" - {
      val locationType = "A"

      "when phase 5" - {
        def url(locationType: String) = s"/$baseUrl/lists/TypeOfLocation?data.type=$locationType"
        "must return Seq of security types when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(locationType)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(locationTypeResponseJson))
              )

              val expectedResult = LocationType("A", "Designated location")

              connector.getTypeOfLocation(locationType).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(locationType), emptyPhase5ResponseJson, connector.getTypeOfLocation(locationType))
          }
        }

        "must handle client and server errors for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(locationType), connector.getTypeOfLocation(locationType))
          }
        }
      }

      "when phase 6" - {
        def url(locationType: String) = s"/$baseUrl/lists/TypeOfLocation?keys=$locationType"
        "must return Seq of security types when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(locationType)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(locationTypeResponseP6Json))
              )

              val expectedResult = LocationType("A", "Designated location")

              connector.getTypeOfLocation(locationType).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(locationType), emptyPhase6ResponseJson, connector.getTypeOfLocation(locationType))
          }
        }

        "must handle client and server errors for control types" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(locationType), connector.getTypeOfLocation(locationType))
          }
        }
      }
    }

    "getCustomsOfficesOfTransitForCountry" - {

      "when phase 5" - {
        def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=TRA"

        "must return a successful future response with a sequence of CustomsOffices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val countryId = "GB"

              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB1", "testName1", None),
                CustomsOffice("GB2", "testName2", None)
              )

              connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AR"
              checkNoReferenceDataFoundResponse(url(countryId), emptyPhase5ResponseJson, connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)))
          }

        }

        "must handle client and server errors for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              checkErrorResponse(url(countryId), connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)))
          }
        }
      }
    }

    "getCustomsOfficeForId" - {
      "when phase 5" - {
        def url(officeId: String) = s"/$baseUrl/lists/CustomsOffices?data.id=$officeId"

        "must return a successful future response with a sequence of CustomsOffices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val id = "GB1"

              server.stubFor(
                get(urlEqualTo(url(id)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = CustomsOffice("GB1", "testName1", None)

              connector.getCustomsOfficeForId(id).futureValue.value mustEqual expectedResult

          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val id        = "GB3"
              checkNoReferenceDataFoundResponse(url(id), emptyPhase5ResponseJson, connector.getCustomsOfficeForId(id))
          }

        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val id        = "GB1"
              checkErrorResponse(url(id), connector.getCustomsOfficeForId(id))
          }
        }
      }
    }

    "getCustomsOfficesForIds" - {
      def url = s"/$baseUrl/lists/CustomsOffices?data.id=GB1&data.id=GB2"
      val ids = Seq("GB1", "GB2")

      "when phase 5" - {
        "must return a successful future response with a sequence of CustomsOffices" in {

          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB1", "testName1", None),
                CustomsOffice("GB2", "testName2", None)
              )

              connector.getCustomsOfficesForIds(ids).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCustomsOfficesForIds(ids))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCustomsOfficesForIds(ids))
          }
        }
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {

      "when phase 5" - {
        def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DES"

        "must return a successful future response with a sequence of CustomsOffices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val countryId = "GB"

              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB1", "testName1", None),
                CustomsOffice("GB2", "testName2", None)
              )

              connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AR"
              checkNoReferenceDataFoundResponse(url(countryId),
                                                emptyPhase5ResponseJson,
                                                connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId))
              )
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)))
          }
        }
      }
    }

    "getNationalities" - {
      val url: String = s"/$baseUrl/lists/Nationality"

      "when phase 5" - {
        "must return Seq of Country when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(nationalitiesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                Nationality("AR", "Argentina"),
                Nationality("AU", "Australia")
              )

              connector.getNationalities().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getNationalities())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getNationalities())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Country when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(nationalitiesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                Nationality("AR", "Argentina"),
                Nationality("AU", "Australia")
              )

              connector.getNationalities().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getNationalities())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getNationalities())
          }
        }
      }
    }

    "getNationality" - {
      val code = "AR"

      "when phase 5" - {
        def url(code: String): String = s"/$baseUrl/lists/Nationality?data.code=$code"
        "must return a Nationality when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(nationalityResponseJson))
              )

              val expectedResult = Nationality("AR", "Argentina")

              connector.getNationality(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase5ResponseJson, connector.getNationality(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getNationality(code))
          }
        }
      }

      "when phase 6" - {
        def url(code: String): String = s"/$baseUrl/lists/Nationality?keys=$code"
        "must return a Nationality when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(nationalityResponseP6Json))
              )

              val expectedResult = Nationality("AR", "Argentina")

              connector.getNationality(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase6ResponseJson, connector.getNationality(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getNationality(code))
          }
        }
      }

    }

    "getTransportModeCodes" - {
      val url: String = s"/$baseUrl/lists/TransportModeCode"

      "when inland modes" - {

        "when phase 5" - {
          "must return Seq of inland modes when successful" in {
            val responseJson: String =
              """
                |{
                |  "_links": {
                |    "self": {
                |      "href": "/customs-reference-data/lists/TransportModeCode"
                |    }
                |  },
                |  "meta": {
                |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
                |    "snapshotDate": "2023-01-01"
                |  },
                |  "id": "TransportModeCode",
                |  "data": [
                |    {
                |      "code": "1",
                |      "description": "Maritime Transport"
                |    },
                |    {
                |      "code": "2",
                |      "description": "Rail Transport"
                |    }
                |  ]
                |}
                |""".stripMargin

            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]

                server.stubFor(
                  get(urlEqualTo(url))
                    .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                    .willReturn(okJson(responseJson))
                )

                val expectedResult = NonEmptySet.of(
                  InlandMode("1", "Maritime Transport"),
                  InlandMode("2", "Rail Transport")
                )

                connector.getInlandModes().futureValue.value mustEqual expectedResult
            }
          }

          "must throw a NoReferenceDataFoundException for an empty response" in {
            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getInlandModes())
            }

          }

          "must return an exception when an error response is returned" in {
            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkErrorResponse(url, connector.getInlandModes())
            }
          }
        }

        "when phase 6" - {
          "must return Seq of inland modes when successful" in {
            val responseJson: String =
              """
                |[
                |    {
                |      "key": "1",
                |      "value": "Maritime Transport"
                |    },
                |    {
                |      "key": "2",
                |      "value": "Rail Transport"
                |    }
                |]
                |""".stripMargin

            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]

                server.stubFor(
                  get(urlEqualTo(url))
                    .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                    .willReturn(okJson(responseJson))
                )

                val expectedResult = NonEmptySet.of(
                  InlandMode("1", "Maritime Transport"),
                  InlandMode("2", "Rail Transport")
                )

                connector.getInlandModes().futureValue.value mustEqual expectedResult
            }
          }

          "must throw a NoReferenceDataFoundException for an empty response" in {
            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getInlandModes())
            }

          }

          "must return an exception when an error response is returned" in {
            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkErrorResponse(url, connector.getInlandModes())
            }
          }
        }

      }

      "when border modes" - {

        "when phase 5" - {
          "must return Seq of border modes when successful" in {
            val responseJson: String =
              """
                |{
                |  "_links": {
                |    "self": {
                |      "href": "/customs-reference-data/lists/TransportModeCode"
                |    }
                |  },
                |  "meta": {
                |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
                |    "snapshotDate": "2023-01-01"
                |  },
                |  "id": "TransportModeCode",
                |  "data": [
                |    {
                |      "code": "1",
                |      "description": "Maritime Transport"
                |    },
                |    {
                |      "code": "1",
                |      "description": "Maritime Transport"
                |    },
                |    {
                |      "code": "2",
                |      "description": "Rail Transport"
                |    }
                |  ]
                |}
                |""".stripMargin

            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                server.stubFor(
                  get(urlEqualTo(url))
                    .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                    .willReturn(okJson(responseJson))
                )

                val expectedResult = NonEmptySet.of(
                  BorderMode("1", "Maritime Transport"),
                  BorderMode("2", "Rail Transport")
                )

                connector.getBorderModes().futureValue.value mustEqual expectedResult
            }
          }

          "must throw a NoReferenceDataFoundException for an empty response" in {
            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getBorderModes())
            }

          }

          "must return an exception when an error response is returned" in {
            running(phase5App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkErrorResponse(url, connector.getBorderModes())
            }
          }
        }

        "when phase 6" - {
          "must return Seq of border modes when successful" in {
            val responseJson: String =
              """
                |[
                |    {
                |      "key": "1",
                |      "value": "Maritime Transport"
                |    },
                |    {
                |      "key": "1",
                |      "value": "Maritime Transport"
                |    },
                |    {
                |      "key": "2",
                |      "value": "Rail Transport"
                |    }
                |]
                |""".stripMargin

            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                server.stubFor(
                  get(urlEqualTo(url))
                    .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                    .willReturn(okJson(responseJson))
                )

                val expectedResult = NonEmptySet.of(
                  BorderMode("1", "Maritime Transport"),
                  BorderMode("2", "Rail Transport")
                )

                connector.getBorderModes().futureValue.value mustEqual expectedResult
            }
          }

          "must throw a NoReferenceDataFoundException for an empty response" in {
            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getBorderModes())
            }

          }

          "must return an exception when an error response is returned" in {
            running(phase6App) {
              app =>
                val connector = app.injector.instanceOf[ReferenceDataConnector]
                checkErrorResponse(url, connector.getBorderModes())
            }
          }
        }
      }
    }

    "getQualifierOfTheIdentifications" - {
      val url: String = s"/$baseUrl/lists/QualifierOfTheIdentification"

      "when phase 5" - {
        "must return Seq of Identification qualifiers when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(locationOfGoodsIdentificationResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                LocationOfGoodsIdentification("T", "Postal code"),
                LocationOfGoodsIdentification("X", "EORI number")
              )

              connector.getQualifierOfTheIdentifications().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getQualifierOfTheIdentifications())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfTheIdentifications())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Identification qualifiers when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(locationOfGoodsIdentificationResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                LocationOfGoodsIdentification("T", "Postal code"),
                LocationOfGoodsIdentification("X", "EORI number")
              )

              connector.getQualifierOfTheIdentifications().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getQualifierOfTheIdentifications())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfTheIdentifications())
          }
        }
      }
    }

    "getCustomsOfficesOfExitForCountry" - {

      "when phase 5" - {
        def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=EXT"

        "must return a successful future response with a sequence of CustomsOffices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val countryId = "GB"

              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB1", "testName1", None),
                CustomsOffice("GB2", "testName2", None)
              )

              connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val countryId = "AR"
              checkNoReferenceDataFoundResponse(url(countryId), emptyPhase5ResponseJson, connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              checkErrorResponse(url(countryId), connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)))
          }
        }
      }
    }

    "getCustomsOfficesOfDepartureForCountry" - {
      "when phase 5" - {
        def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DEP"

        "must return a successful future response with a sequence of CustomsOffices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              val countryId = "GB"

              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB1", "testName1", None),
                CustomsOffice("GB2", "testName2", None)
              )

              connector.getCustomsOfficesOfDepartureForCountry(countryId).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AR"
              checkNoReferenceDataFoundResponse(url(countryId), emptyPhase5ResponseJson, connector.getCustomsOfficesOfDepartureForCountry(countryId))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDepartureForCountry(countryId))
          }
        }
      }
    }

    "getCountries for full list" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "when phase 5" - {
        "must return Seq of Country when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
              )

              val expectedResult = NonEmptySet.of(
                Country(CountryCode("GB"), "United Kingdom"),
                Country(CountryCode("AD"), "Andorra")
              )
              connector.getCountries("CountryCodesFullList").futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCountries("CountryCodesFullList"))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountries("CountryCodesFullList"))
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Country when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countriesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                Country(CountryCode("GB"), "United Kingdom"),
                Country(CountryCode("AD"), "Andorra")
              )
              connector.getCountries("CountryCodesFullList").futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCountries("CountryCodesFullList"))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountries("CountryCodesFullList"))
          }
        }
      }
    }

    "getAddressPostcodeBasedCountries" - {
      val url = s"/$baseUrl/lists/CountryAddressPostcodeBased"

      "when phase 5" - {
        "must return Seq of Country when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(countriesResponseJson("CountryAddressPostcodeBased")))
              )

              val expectedResult = NonEmptySet.of(
                Country(CountryCode("GB"), "United Kingdom"),
                Country(CountryCode("AD"), "Andorra")
              )

              connector.getAddressPostcodeBasedCountries().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getAddressPostcodeBasedCountries())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAddressPostcodeBasedCountries())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Country when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]

              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countriesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                Country(CountryCode("GB"), "United Kingdom"),
                Country(CountryCode("AD"), "Andorra")
              )

              connector.getAddressPostcodeBasedCountries().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getAddressPostcodeBasedCountries())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getAddressPostcodeBasedCountries())
          }
        }
      }
    }

    "getCountriesWithoutZipCountry" - {

      "when phase 5" - {
        def url(countryId: String) = s"/$baseUrl/lists/CountryWithoutZip?data.code=$countryId"
        "must return Seq of Country when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(countryResponseJson))
              )

              val expectedResult = CountryCode(countryId)

              connector.getCountriesWithoutZipCountry(countryId).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AD"
              checkNoReferenceDataFoundResponse(url(countryId), emptyPhase5ResponseJson, connector.getCountriesWithoutZipCountry(countryId))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AD"
              checkErrorResponse(url(countryId), connector.getCountriesWithoutZipCountry(countryId))
          }
        }
      }

      "when phase 6" - {
        def url(countryId: String) = s"/$baseUrl/lists/CountryWithoutZip?keys=$countryId"
        "must return Seq of Country when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "GB"
              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countryResponseP6Json))
              )

              val expectedResult = CountryCode(countryId)

              connector.getCountriesWithoutZipCountry(countryId).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AD"
              checkNoReferenceDataFoundResponse(url(countryId), emptyPhase6ResponseJson, connector.getCountriesWithoutZipCountry(countryId))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AD"
              checkErrorResponse(url(countryId), connector.getCountriesWithoutZipCountry(countryId))
          }
        }
      }
    }

    "getUnLocodes" - {
      val url = s"/$baseUrl/lists/UnLocodeExtended"

      "when phase 5" - {
        "must return Seq of UN/LOCODES when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(unLocodesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                UnLocode("UN1", "testName1"),
                UnLocode("UN2", "testName2")
              )

              connector.getUnLocodes().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getUnLocodes())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getUnLocodes())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of UN/LOCODES when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(unLocodesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                UnLocode("UN1", "testName1"),
                UnLocode("UN2", "testName2")
              )

              connector.getUnLocodes().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getUnLocodes())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getUnLocodes())
          }
        }
      }
    }

    "getUnLocode" - {
      val code = "UN1"

      "when phase 5" - {

        val url = s"/$baseUrl/lists/UnLocodeExtended?data.unLocodeExtendedCode=UN1"

        "must return a Seq of UN/LOCODES when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(unLocodeResponseJson))
              )

              val expectedResult = UnLocode("UN1", "testName1")

              connector.getUnLocode(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getUnLocode(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getUnLocode(code))
          }
        }
      }

      "when phase 6" - {

        val url = s"/$baseUrl/lists/UnLocodeExtended?keys=UN1"

        "must return a Seq of UN/LOCODES when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(unLocodeResponseP6Json))
              )

              val expectedResult = UnLocode("UN1", "testName1")

              connector.getUnLocode(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getUnLocode(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getUnLocode(code))
          }
        }
      }
    }

    "getSpecificCircumstanceIndicators" - {
      val url = s"/$baseUrl/lists/SpecificCircumstanceIndicatorCode"

      "when phase 5" - {
        "must return Seq of specific circumstance indicators when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(specificCircumstanceIndicatorsResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                SpecificCircumstanceIndicator("SCI1", "testName1"),
                SpecificCircumstanceIndicator("SCI2", "testName2")
              )

              connector.getSpecificCircumstanceIndicators().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getSpecificCircumstanceIndicators())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of specific circumstance indicators when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(specificCircumstanceIndicatorsResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                SpecificCircumstanceIndicator("SCI1", "testName1"),
                SpecificCircumstanceIndicator("SCI2", "testName2")
              )

              connector.getSpecificCircumstanceIndicators().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getSpecificCircumstanceIndicators())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
          }
        }
      }
    }

    "getMeansOfTransportIdentificationTypesActive" - {
      val url: String = s"/$baseUrl/lists/TypeOfIdentificationofMeansOfTransportActive"

      "when phase 5" - {
        "must return Seq of Identification when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypesActiveResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                Identification("10", "IMO Ship Identification Number"),
                Identification("11", "Name of the sea-going vessel")
              )

              connector.getMeansOfTransportIdentificationTypesActive().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getMeansOfTransportIdentificationTypesActive())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypesActive())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Identification when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypesActiveResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                Identification("10", "IMO Ship Identification Number"),
                Identification("11", "Name of the sea-going vessel")
              )

              connector.getMeansOfTransportIdentificationTypesActive().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getMeansOfTransportIdentificationTypesActive())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypesActive())
          }
        }
      }
    }

    "getMeansOfTransportIdentificationTypeActive" - {
      val code = "10"
      "when phase 5" - {
        def url(code: String): String = s"/$baseUrl/lists/TypeOfIdentificationofMeansOfTransportActive?data.code=$code"
        "must return Seq of Identification when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypeActiveResponseJson))
              )

              val expectedResult = Identification("10", "IMO Ship Identification Number")

              connector.getMeansOfTransportIdentificationTypeActive(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase5ResponseJson, connector.getMeansOfTransportIdentificationTypeActive(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getMeansOfTransportIdentificationTypeActive(code))
          }
        }
      }

      "when phase 6" - {
        def url(code: String): String = s"/$baseUrl/lists/TypeOfIdentificationofMeansOfTransportActive?keys=$code"
        "must return Seq of Identification when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypeActiveResponseP6Json))
              )

              val expectedResult = Identification("10", "IMO Ship Identification Number")

              connector.getMeansOfTransportIdentificationTypeActive(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase6ResponseJson, connector.getMeansOfTransportIdentificationTypeActive(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getMeansOfTransportIdentificationTypeActive(code))
          }
        }
      }
    }

    "getMeansOfTransportIdentificationTypes" - {
      val url: String = s"/$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport"

      "when phase 5" - {
        "must return Seq of Identification when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                TransportMeansIdentification("10", "IMO Ship Identification Number"),
                TransportMeansIdentification("11", "Name of the sea-going vessel")
              )

              connector.getMeansOfTransportIdentificationTypes().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getMeansOfTransportIdentificationTypes())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypes())
          }
        }
      }

      "when phase 6" - {
        "must return Seq of Identification when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypesResponseP6Json))
              )

              val expectedResult = NonEmptySet.of(
                TransportMeansIdentification("10", "IMO Ship Identification Number"),
                TransportMeansIdentification("11", "Name of the sea-going vessel")
              )

              connector.getMeansOfTransportIdentificationTypes().futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getMeansOfTransportIdentificationTypes())
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getMeansOfTransportIdentificationTypes())
          }
        }
      }
    }

    "getMeansOfTransportIdentificationType" - {
      val code = "10"
      "when phase 5" - {
        def url(code: String): String = s"/$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?data.type=$code"

        "must return Seq of Identification when successful" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypeResponseJson))
              )

              val expectedResult = TransportMeansIdentification("10", "IMO Ship Identification Number")

              connector.getMeansOfTransportIdentificationType(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase5ResponseJson, connector.getMeansOfTransportIdentificationType(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getMeansOfTransportIdentificationType(code))
          }
        }
      }

      "when phase 6" - {
        def url(code: String): String = s"/$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?keys=$code"

        "must return Seq of Identification when successful" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url(code)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(meansOfTransportIdentificationTypeResponseP6Json))
              )

              val expectedResult = TransportMeansIdentification("10", "IMO Ship Identification Number")

              connector.getMeansOfTransportIdentificationType(code).futureValue.value mustEqual expectedResult
          }
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url(code), emptyPhase6ResponseJson, connector.getMeansOfTransportIdentificationType(code))
          }
        }

        "must return an exception when an error response is returned" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url(code), connector.getMeansOfTransportIdentificationType(code))
          }
        }
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, json: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe a[Exception]
    }
  }
}
