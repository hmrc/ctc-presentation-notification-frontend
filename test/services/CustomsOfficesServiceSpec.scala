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
import cats.data.NonEmptyList
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.{CountryCode, CustomsOffice}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new CustomsOfficesService(mockRefDataConnector)

  val gbCustomsOffice1: CustomsOffice               = CustomsOffice("GB1", "BOSTON", None)
  val gbCustomsOffice2: CustomsOffice               = CustomsOffice("GB2", "Appledore", None)
  val gbCustomsOffices: NonEmptyList[CustomsOffice] = NonEmptyList(gbCustomsOffice1, List(gbCustomsOffice2))

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CustomsOfficesService" - {

    "getCustomsOfficesOfTransitForCountry" - {
      "must return a list of sorted customs offices of transit for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesOfTransitForCountry(eqTo(CountryCode("GB")))(any(), any()))
          .thenReturn(Future.successful(gbCustomsOffices))

        service.getCustomsOfficesOfTransitForCountry(CountryCode("GB")).futureValue.values mustBe
          Seq(gbCustomsOffice2, gbCustomsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesOfTransitForCountry(eqTo(CountryCode("GB")))(any(), any())
      }
    }

    "getCustomsOfficeById" - {
      "must return the head of the customs office list" in {

        when(mockRefDataConnector.getCustomsOfficeForId(any())(any(), any()))
          .thenReturn(Future.successful(gbCustomsOffices))

        service.getCustomsOfficeById("GB1").futureValue mustBe Some(gbCustomsOffice1)

        verify(mockRefDataConnector).getCustomsOfficeForId(any())(any(), any())
      }

      "must return None for empty list" in {

        when(mockRefDataConnector.getCustomsOfficeForId(any())(any(), any()))
          .thenReturn(Future.failed(new NoReferenceDataFoundException))

        service.getCustomsOfficeById("GB1").futureValue mustBe None

        verify(mockRefDataConnector).getCustomsOfficeForId(any())(any(), any())
      }
    }

    "getCustomsOfficeByMultipleIds" - {
      "must customs office list for multiple ids" in {

        when(mockRefDataConnector.getCustomsOfficeForId(eqTo("GB1"))(any(), any()))
          .thenReturn(Future.successful(NonEmptyList(gbCustomsOffice1, Nil)))

        when(mockRefDataConnector.getCustomsOfficeForId(eqTo("GB2"))(any(), any()))
          .thenReturn(Future.successful(NonEmptyList(gbCustomsOffice2, Nil)))

        service.getCustomsOfficesByMultipleIds(Seq("GB1", "GB2")).futureValue mustBe gbCustomsOffices.toList

        verify(mockRefDataConnector, times(2)).getCustomsOfficeForId(any())(any(), any())
      }

      "must return empty list when given an empty list" in {
        service.getCustomsOfficesByMultipleIds(Nil).futureValue mustBe Seq.empty

        verify(mockRefDataConnector, times(0)).getCustomsOfficeForId(any())(any(), any())
      }

      "must return empty list for non matching" in {

        when(mockRefDataConnector.getCustomsOfficeForId(any())(any(), any()))
          .thenReturn(Future.failed(new NoReferenceDataFoundException))

        service.getCustomsOfficesByMultipleIds(Seq("GB1", "GB2")).futureValue mustBe Seq.empty

        verify(mockRefDataConnector, times(2)).getCustomsOfficeForId(any())(any(), any())
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {
      "must return a list of sorted customs offices of destination for a given country" in {

        when(mockRefDataConnector.getCustomsOfficesOfDestinationForCountry(eqTo(CountryCode("GB")))(any(), any()))
          .thenReturn(Future.successful(gbCustomsOffices))

        service.getCustomsOfficesOfDestinationForCountry(CountryCode("GB")).futureValue.values mustBe
          Seq(gbCustomsOffice2, gbCustomsOffice1)

        verify(mockRefDataConnector).getCustomsOfficesOfDestinationForCountry(eqTo(CountryCode("GB")))(any(), any())
      }
    }
  }
}
