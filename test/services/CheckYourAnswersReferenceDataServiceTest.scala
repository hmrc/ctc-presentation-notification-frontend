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
import models.reference.Nationality
import models.reference.transport.border.active.Identification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
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
      val identification1 = Identification("code1", "description")
      val identification2 = Identification("code2", "description")
      val identifications = Seq(identification1, identification2)

      when(connector.getMeansOfTransportIdentificationTypesActive()(any(), any())).thenReturn(Future.successful(identifications))

      service.getBorderMeansIdentification("code1").futureValue mustBe identification1
      an[Exception] mustBe thrownBy(service.getBorderMeansIdentification("code3").futureValue)
    }

    "getNationality" in {
      val nationality1  = Nationality("code1", "description")
      val nationality2  = Nationality("code2", "description")
      val nationalities = Seq(nationality1, nationality2)

      when(connector.getNationalities()(any(), any())).thenReturn(Future.successful(nationalities))

      service.getNationality("code1").futureValue mustBe nationality1
      an[Exception] mustBe thrownBy(service.getBorderMeansIdentification("code3").futureValue)
    }
  }
}
