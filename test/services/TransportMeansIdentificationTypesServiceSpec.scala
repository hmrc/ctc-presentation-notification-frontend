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
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import generators.Generators
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportMeansIdentificationTypesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector             = mock[ReferenceDataConnector]
  private val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]
  private val service = new TransportMeansIdentificationTypesService(mockRefDataConnector, mockTransportModeCodesService)

  private val identification1 = TransportMeansIdentification("41", "Registration number of an aircraft")
  private val identification2 = TransportMeansIdentification("40", "IATA flight number")
  private val identification3 = TransportMeansIdentification("30", "Registration number of a road vehicle")
  private val identification4 = TransportMeansIdentification("21", "Train number")
  private val identification5 = TransportMeansIdentification("11", "Name of a sea-going vessel")
  private val identification6 = TransportMeansIdentification("10", "IMO ship identification number")
  private val identification7 = TransportMeansIdentification("99", "Unknown – Valid only during the Transitional Period")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "TransportMeansIdentificationTypesService" - {

    val request = DataRequest(fakeRequest, eoriNumber, emptyUserAnswers)

    "getMeansOfTransportIdentificationTypes" - {
      "must return a list of sorted identification types beginning with number 1 and exclude Unknown identification when inlandModeOfTransport is Maritime" in {
        val inlandMode = InlandMode("1", "Maritime")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification5, identification6, identification7)))

        service.getMeansOfTransportIdentificationTypes(index, Some(inlandMode))(hc, request).futureValue mustBe
          Seq(identification6, identification5)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 2 and exclude Unknown identification when inlandModeOfTransport is Rail" in {
        val inlandMode = InlandMode("2", "Rail")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification4, identification7)))

        service.getMeansOfTransportIdentificationTypes(index, Some(inlandMode))(hc, request).futureValue mustBe
          Seq(identification4)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 3 and exclude Unknown identification when inlandModeOfTransport is Road" in {
        val inlandMode = InlandMode("3", "Road")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification3, identification7)))

        service.getMeansOfTransportIdentificationTypes(index, Some(inlandMode))(hc, request).futureValue mustBe
          Seq(identification3)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 4 and exclude Unknown identification when inlandModeOfTransport is Air" in {
        val inlandMode = InlandMode("4", "Air")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification1, identification2, identification7)))

        service.getMeansOfTransportIdentificationTypes(index, Some(inlandMode))(hc, request).futureValue mustBe
          Seq(identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types excluding Unknown identification when inlandModeOfTransport is None" in {
        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(
            Future.successful(
              NonEmptySet.of(identification1, identification2, identification3, identification4, identification5, identification6, identification7)
            )
          )

        when(mockTransportModeCodesService.getInlandModes()).thenReturn(Future.successful(Seq(InlandMode("1", "Maritime"))))

        service.getMeansOfTransportIdentificationTypes(index, None)(hc, request).futureValue mustBe
          Seq(identification6, identification5, identification4, identification3, identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }
    }
  }
}
