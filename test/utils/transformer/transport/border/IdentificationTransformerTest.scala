/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformer.transport.border

import base.SpecBase
import base.TestMessageData.activeBorderTransportMeansIdentification
import models.Index
import models.reference.transport.border.active.Identification
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.transport.border.active.IdentificationPage
import services.MeansOfTransportIdentificationTypesActiveService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentificationTransformerTest extends SpecBase {
  private val service     = mock[MeansOfTransportIdentificationTypesActiveService]
  private val transformer = new IdentificationTransformer(service)

  override def beforeEach() =
    reset(service)

  "IdentificationTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        val identification = Identification(activeBorderTransportMeansIdentification, "description")
        when(service.getMeansOfTransportIdentificationTypesActive).thenReturn(Future.successful(Seq(identification)))

        val userAnswers = emptyUserAnswers
        val index       = Index(0)
        userAnswers.get(IdentificationPage(index)) mustBe None

        whenReady(transformer.transform(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(IdentificationPage(index)) mustBe Some(identification)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      val identification = Identification("something else", "description")
      when(service.getMeansOfTransportIdentificationTypesActive).thenReturn(Future.successful(Seq(identification)))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(IdentificationPage(index)) mustBe None

      whenReady(transformer.transform(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(IdentificationPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(service.getMeansOfTransportIdentificationTypesActive).thenReturn(Future.failed(new RuntimeException("")))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(IdentificationPage(index)) mustBe None

      whenReady[Throwable, Assertion](transformer.transform(userAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
