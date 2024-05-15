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

package utils.transformer.transport

import base.SpecBase
import generators.Generators
import models.reference.TransportMode.InlandMode
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.InlandModePage
import services.TransportModeCodesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InlandModeTransformerTest extends SpecBase with Generators {
  private val service     = mock[TransportModeCodesService]
  private val transformer = new InlandModeTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "InlandModeTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        forAll(arbitrary[InlandMode]) {
          inlandMode =>
            when(service.getInlandModes())
              .thenReturn(Future.successful(Seq(inlandMode)))

            val userAnswers = setInlandModeOfTransportOnUserAnswersLens.set(
              Some(inlandMode.code)
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(InlandModePage).value mustBe inlandMode
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      forAll(arbitrary[InlandMode]) {
        inlandMode =>
          when(service.getInlandModes())
            .thenReturn(Future.successful(Nil))

          val userAnswers = setInlandModeOfTransportOnUserAnswersLens.set(
            Some(inlandMode.code)
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(InlandModePage) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(service.getInlandModes())
        .thenReturn(Future.failed(new RuntimeException("")))

      whenReady[Throwable, Assertion](transformer.transform.apply(emptyUserAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
