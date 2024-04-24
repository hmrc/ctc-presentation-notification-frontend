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
import models.EoriNumber
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MeansOfTransportIdentificationTypesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  implicit val dataRequest: DataRequest[AnyContent]                    = DataRequest[AnyContent](request = FakeRequest("", ""), eoriNumber = EoriNumber("123"), emptyUserAnswers)
  private val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]

  private val service = new MeansOfTransportIdentificationTypesService(mockRefDataConnector, mockTransportModeCodesService)

  private val identification1 = TransportMeansIdentification("12", "Night Bus")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MeansOfTransportIdentificationTypesService" - {

    "getMeansOfTransportIdentificationTypes" - {
      "must return a list of sorted identification types " in {
        val inlandMode = InlandMode("1", "Bus")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification1)))

        service.getMeansOfTransportIdentificationTypes(Some(inlandMode)).futureValue mustBe
          Seq(identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())

      }

      "must return a list of sorted identification types when None" in {
        val inlandMode = InlandMode("1", "Bus")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(identification1)))

        when(mockRefDataConnector.getTransportModeCodes[InlandMode]()(any(), any(), any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(inlandMode)))

        when(mockTransportModeCodesService.getInlandModes()(any()))
          .thenReturn(Future.successful(Seq(inlandMode)))

        service.getMeansOfTransportIdentificationTypes(None).futureValue mustBe
          Seq(identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())

      }

    }
  }
}
