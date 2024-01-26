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

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import helper.WireMockServerHandler
import models.LocationType
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customs-reference-data.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

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
      |      "LanguageCode": "EN",
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
      |      "LanguageCode": "ES",
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

  private val locationTypesResponseJson: String =
    """
      |
      |   {
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
      |   }
      |""".stripMargin

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  "Reference Data" - {

    "getTypesOfLocation" - {
      val url = s"/$baseUrl/lists/TypeOfLocation"
      "must return Seq of security types when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(locationTypesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          LocationType("A", "Designated location"),
          LocationType("B", "Authorised place")
        )

        connector.getTypesOfLocation().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTypesOfLocation())
      }

      "must handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getTypesOfLocation())
      }
    }

    "getCustomsOfficesOfTransitForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=TRA"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)))
      }

      "must handle client and server errors for control types" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)))
      }
    }

    "getCustomsOfficeForId" - {
      def url(officeId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.id=$officeId"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val id = "GB1"

        server.stubFor(
          get(urlEqualTo(url(id)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficeForId(id).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val id = "GB3"
        checkNoReferenceDataFoundResponse(url(id), connector.getCustomsOfficeForId(id))
      }

      "must return an exception when an error response is returned" in {
        val id = "GB1"
        checkErrorResponse(url(id), connector.getCustomsOfficeForId(id))
      }
    }

    "getCustomsOfficesForIds" - {
      def url = s"/$baseUrl/filtered-lists/CustomsOffices?data.id=GB1&data.id=GB2"
      val ids = Seq("GB1", "GB2")

      "must return a successful future response with a sequence of CustomsOffices" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesForIds(ids).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOfficesForIds(ids))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCustomsOfficesForIds(ids))
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DES"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)))
      }
    }

    "getNationalities" - {
      val url: String = s"/$baseUrl/lists/Nationality"

      "must return Seq of Country when successful" in {
        val nationalitiesResponseJson: String =
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

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(nationalitiesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          Nationality("AR", "Argentina"),
          Nationality("AU", "Australia")
        )

        connector.getNationalities().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getNationalities())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getNationalities())
      }
    }

    "getTransportModeCodes" - {
      val url: String = s"/$baseUrl/lists/TransportModeCode"

      "when inland modes" - {

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

          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            InlandMode("1", "Maritime Transport"),
            InlandMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[InlandMode]().futureValue mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[InlandMode]())
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getTransportModeCodes[InlandMode]())
        }
      }

      "when border modes" - {

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

          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(responseJson))
          )

          val expectedResult = NonEmptySet.of(
            BorderMode("1", "Maritime Transport"),
            BorderMode("2", "Rail Transport")
          )

          connector.getTransportModeCodes[BorderMode]().futureValue mustEqual expectedResult
        }

        "must throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getTransportModeCodes[BorderMode]())
        }

        "must return an exception when an error response is returned" in {
          checkErrorResponse(url, connector.getTransportModeCodes[BorderMode]())
        }
      }
    }

    "getCustomsOfficesOfExitForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=EXT"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)))
      }
    }

    "getCustomsOfficesOfDepartureForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DEP"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfDepartureForCountry(countryId).futureValue mustBe expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCustomsOfficesOfDepartureForCountry(countryId))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDepartureForCountry(countryId))
      }
    }

    "getCountries for full list" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries("CountryCodesFullList").futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries("CountryCodesFullList"))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountries("CountryCodesFullList"))
      }
    }

    "getCustomsSecurityAgreementAreaCountries" - {
      val url = s"/$baseUrl/lists/CountryCustomsSecurityAgreementArea"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCustomsSecurityAgreementArea")))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCustomsSecurityAgreementAreaCountries().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsSecurityAgreementAreaCountries())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCustomsSecurityAgreementAreaCountries())
      }
    }

    "getCountryCodesCTC" - {
      val url = s"/$baseUrl/lists/CountryCodesCTC"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesCTC")))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountryCodesCTC().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountryCodesCTC())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountryCodesCTC())
      }
    }

    "getAddressPostcodeBasedCountries" - {
      val url = s"/$baseUrl/lists/CountryAddressPostcodeBased"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryAddressPostcodeBased")))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getAddressPostcodeBasedCountries().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getAddressPostcodeBasedCountries())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAddressPostcodeBasedCountries())
      }
    }

    "getCountriesWithoutZip" - {
      val url = s"/$baseUrl/lists/CountryWithoutZip"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryWithoutZip")))
        )

        val expectedResult = NonEmptySet.of(
          CountryCode("GB"),
          CountryCode("AD")
        )

        connector.getCountriesWithoutZip().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountriesWithoutZip())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountriesWithoutZip())
      }
    }

    "getUnLocodes" - {
      val url = s"/$baseUrl/lists/UnLocodeExtended"

      "must return Seq of UN/LOCODES when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(unLocodesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          UnLocode("UN1", "testName1"),
          UnLocode("UN2", "testName2")
        )

        connector.getUnLocodes().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getUnLocodes())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getUnLocodes())
      }
    }

    "getSpecificCircumstanceIndicators" - {
      val url = s"/$baseUrl/lists/SpecificCircumstanceIndicatorCode"

      "must return Seq of specific circumstance indicators when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(specificCircumstanceIndicatorsResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          SpecificCircumstanceIndicator("SCI1", "testName1"),
          SpecificCircumstanceIndicator("SCI2", "testName2")
        )

        connector.getSpecificCircumstanceIndicators().futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSpecificCircumstanceIndicators())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[_]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ mustBe a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
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

        whenReady[Throwable, Assertion](result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

}
