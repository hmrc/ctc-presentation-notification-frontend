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

import base.SpecBase
import connectors.ReferenceDataConnector
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.reference.{Country, CountryCode, LocationType, Nationality}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersReferenceDataServiceSpec extends SpecBase {

  private val connector = mock[ReferenceDataConnector]
  private val service   = new CheckYourAnswersReferenceDataService(connector)

  override def beforeEach(): Unit =
    reset(connector)

  "CheckYourAnswersReferenceDataService should" - {

    "getMeansOfTransportIdentificationType" in {
      val identification = TransportMeansIdentification("code", "description")

      when(connector.getMeansOfTransportIdentificationType(any())(any(), any()))
        .thenReturn(Future.successful(Right(identification)))

      service.getMeansOfTransportIdentificationType("code").futureValue mustEqual identification

      verify(connector).getMeansOfTransportIdentificationType(eqTo("code"))(any(), any())
    }

    "getBorderMeansIdentification" in {
      val identification = Identification("code", "description")

      when(connector.getMeansOfTransportIdentificationTypeActive(any())(any(), any()))
        .thenReturn(Future.successful(Right(identification)))

      service.getBorderMeansIdentification("code").futureValue mustEqual identification

      verify(connector).getMeansOfTransportIdentificationTypeActive(eqTo("code"))(any(), any())
    }

    "getLocationType" in {
      val locationType = LocationType("code", "description")

      when(connector.getTypeOfLocation(any())(any(), any()))
        .thenReturn(Future.successful(Right(locationType)))

      service.getLocationType("code").futureValue mustEqual locationType

      verify(connector).getTypeOfLocation(eqTo("code"))(any(), any())
    }

    "getNationality" in {
      val nationality = Nationality("code", "description")

      when(connector.getNationality(any())(any(), any()))
        .thenReturn(Future.successful(Right(nationality)))

      service.getNationality("code").futureValue mustEqual nationality

      verify(connector).getNationality(eqTo("code"))(any(), any())
    }

    "getCountry" in {
      val country = Country(CountryCode("code"), "description")

      when(connector.getCountry(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(country)))

      service.getCountry("code").futureValue mustEqual country

      verify(connector).getCountry(any(), eqTo("code"))(any(), any())
    }
  }
}
