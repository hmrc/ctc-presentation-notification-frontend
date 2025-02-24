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
import models.Index
import models.reference.TransportMode.BorderMode
import models.reference.transport.border.active.Identification
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MeansOfTransportIdentificationTypesActiveServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new MeansOfTransportIdentificationTypesActiveService(mockRefDataConnector)

  private val borderModeOfTransport = arbitrary[BorderMode].sample.value

  private val identification1 = Identification("41", "Registration number of an aircraft")
  private val identification2 = Identification("40", "IATA flight number")
  private val identification3 = Identification("30", "Registration number of a road vehicle")
  private val identification4 = Identification("21", "Train number")
  private val identification5 = Identification("11", "Name of a sea-going vessel")
  private val identification6 = Identification("10", "IMO ship identification number")
  private val identification7 = Identification("99", "Unknown – Valid only during the Transitional Period")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MeansOfTransportIdentificationTypesService" - {

    "getMeansOfTransportIdentificationTypes" - {
      "when it is the first index position" - {
        "must return a list of sorted identification types beginning with number 1 and exclude Unknown identification when BorderModeOfTransport is Sea" in {
          val borderMode = BorderMode("1", "Maritime")

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(Future.successful(Right(NonEmptySet.of(identification5, identification6, identification7))))

          service.getMeansOfTransportIdentificationTypesActive(index, Some(borderMode)).futureValue mustBe
            Seq(identification6, identification5)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }

        "must return a list of sorted identification types beginning with number 2 and exclude Unknown identification when BorderModeOfTransport is ChannelTunnel" in {
          val borderMode = BorderMode("2", "Rail")

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(Future.successful(Right(NonEmptySet.of(identification4, identification7))))

          service.getMeansOfTransportIdentificationTypesActive(index, Some(borderMode)).futureValue mustBe
            Seq(identification4)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }

        "must return a list of sorted identification types beginning with number 3 and exclude Unknown identification when BorderModeOfTransport is IrishLandBoundary" in {
          val borderMode = BorderMode("3", "Road")

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(Future.successful(Right(NonEmptySet.of(identification3, identification7))))

          service.getMeansOfTransportIdentificationTypesActive(index, Some(borderMode)).futureValue mustBe
            Seq(identification3)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }

        "must return a list of sorted identification types beginning with number 4 and exclude Unknown identification when BorderModeOfTransport is Air" in {
          val borderMode = BorderMode("4", "Air")

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(Future.successful(Right(NonEmptySet.of(identification1, identification2, identification7))))

          service.getMeansOfTransportIdentificationTypesActive(index, Some(borderMode)).futureValue mustBe
            Seq(identification2, identification1)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }

        "must return a list of sorted identification types excluding Unknown identification when BorderModeOfTransport is None" in {

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(
              Future.successful(
                Right(NonEmptySet.of(identification1, identification2, identification3, identification4, identification5, identification6, identification7))
              )
            )

          service.getMeansOfTransportIdentificationTypesActive(index, None).futureValue mustBe
            Seq(identification6, identification5, identification4, identification3, identification2, identification1)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }
      }

      "when it is not the first index position" - {
        "must return a list of sorted identification types excluding Unknown identification" in {

          when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
            .thenReturn(
              Future.successful(
                Right(NonEmptySet.of(identification1, identification2, identification3, identification4, identification5, identification6, identification7))
              )
            )

          service.getMeansOfTransportIdentificationTypesActive(Index(1), Some(borderModeOfTransport)).futureValue mustBe
            Seq(identification6, identification5, identification4, identification3, identification2, identification1)

          verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
        }
      }

      "must return all identification types when not filtered" in {

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(
            Future.successful(
              Right(NonEmptySet.of(identification1, identification2, identification3, identification4, identification5, identification6, identification7))
            )
          )

        service.getMeansOfTransportIdentificationTypesActive().futureValue mustBe
          Seq(identification6, identification5, identification4, identification3, identification2, identification1, identification7)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }
    }

    "getBorderMeansIdentification" in {
      val identification = Identification("code", "description")

      when(mockRefDataConnector.getMeansOfTransportIdentificationTypeActive(any())(any(), any()))
        .thenReturn(Future.successful(Right(identification)))

      service.getBorderMeansIdentification("code").futureValue mustBe identification

      verify(mockRefDataConnector).getMeansOfTransportIdentificationTypeActive(eqTo("code"))(any(), any())
    }
  }
}
