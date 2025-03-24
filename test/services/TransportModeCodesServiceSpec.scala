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
import models.reference.TransportMode.{BorderMode, InlandMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportModeCodesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new TransportModeCodesService(mockRefDataConnector)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "TransportModeCodesService" - {

    "getBorderModes" - {

      val borderMode1 = BorderMode("8", "Inland waterway")
      val borderMode2 = BorderMode("7", "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods")
      val borderMode3 = BorderMode("5", "Mail (active mode of transport unknown)")
      val borderMode4 = BorderMode("4", "Air")
      val borderMode5 = BorderMode("3", "Road")
      val borderMode6 = BorderMode("2", "Rail")
      val borderMode7 = BorderMode("1", "Maritime")
      val borderMode8 = BorderMode("9", "Mode unknown (Own propulsion)")

      "must return the agreed list of sorted border modes" in {
        when(mockRefDataConnector.getTransportModeCodes[BorderMode]()(any(), any(), any(), any()))
          .thenReturn(
            Future.successful(Right(NonEmptySet.of(borderMode1, borderMode2, borderMode3, borderMode4, borderMode5, borderMode6, borderMode7, borderMode8)))
          )

        service.getBorderModes().futureValue mustBe
          Seq(borderMode7, borderMode6, borderMode5, borderMode4)

        verify(mockRefDataConnector).getTransportModeCodes[BorderMode]()(any(), any(), any(), any())
      }
    }

    "getInlandModeCodes" - {

      val inlandMode1 = InlandMode("8", "Inland waterway")
      val inlandMode2 = InlandMode("7", "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods")
      val inlandMode3 = InlandMode("5", "Mail (active mode of transport unknown)")
      val inlandMode4 = InlandMode("4", "Air")
      val inlandMode5 = InlandMode("3", "Road")
      val inlandMode6 = InlandMode("2", "Rail")
      val inlandMode7 = InlandMode("1", "Maritime")
      val inlandMode8 = InlandMode("9", "Mode unknown (Own propulsion)")

      "must return the agreed list of sorted inland modes" in {
        when(mockRefDataConnector.getTransportModeCodes[InlandMode]()(any(), any(), any(), any()))
          .thenReturn(
            Future.successful(Right(NonEmptySet.of(inlandMode1, inlandMode2, inlandMode3, inlandMode4, inlandMode5, inlandMode6, inlandMode7, inlandMode8)))
          )

        service.getInlandModes().futureValue mustBe
          Seq(inlandMode7, inlandMode6, inlandMode5, inlandMode4, inlandMode3, inlandMode2, inlandMode1)

        verify(mockRefDataConnector).getTransportModeCodes[InlandMode]()(any(), any(), any(), any())
      }
    }
  }
}
