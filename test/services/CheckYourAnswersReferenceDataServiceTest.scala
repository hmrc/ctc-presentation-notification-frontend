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
import models.reference.Nationality
import models.reference.transport.border.active.Identification
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersReferenceDataServiceTest extends SpecBase with BeforeAndAfterEach {

  private val connector = mock[ReferenceDataConnector]
  private val service   = new CheckYourAnswersReferenceDataService(connector)

  override def beforeEach(): Unit =
    reset(connector)

  "CheckYourAnswersReferenceDataService should" - {

    "getBorderMeansIdentification" in {
      val identification = Identification("code", "description")

      when(connector.getMeansOfTransportIdentificationTypeActive(any())(any(), any()))
        .thenReturn(Future.successful(NonEmptySet.of(identification)))

      service.getBorderMeansIdentification("code").futureValue mustBe identification

      verify(connector).getMeansOfTransportIdentificationTypeActive(eqTo("code"))(any(), any())
    }

    "getNationality" in {
      val nationality = Nationality("code", "description")

      when(connector.getNationality(any())(any(), any()))
        .thenReturn(Future.successful(NonEmptySet.of(nationality)))

      service.getNationality("code").futureValue mustBe nationality

      verify(connector).getNationality(eqTo("code"))(any(), any())
    }
  }
}
