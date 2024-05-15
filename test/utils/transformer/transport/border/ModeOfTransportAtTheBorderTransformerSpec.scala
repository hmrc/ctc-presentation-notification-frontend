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
import generators.Generators
import models.reference.TransportMode.BorderMode
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.border.BorderModeOfTransportPage
import services.TransportModeCodesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ModeOfTransportAtTheBorderTransformerSpec extends SpecBase with Generators {
  private val service = mock[TransportModeCodesService]
  val transformer     = new ModeOfTransportAtTheBorderTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "BorderModeOfTransportTransformer" - {
    "must return updated answers with BorderModeOfTransportPage" in {
      forAll(arbitrary[BorderMode]) {
        borderMode =>
          when(service.getBorderModes())
            .thenReturn(Future.successful(Seq(borderMode)))

          val userAnswers = setModeOfTransportAtTheBorderOnUserAnswersLens.set(
            Some(borderMode.code)
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(BorderModeOfTransportPage) mustBe Some(borderMode)
      }
    }

    "must not update if mode of transport is None" in {
      forAll(arbitrary[BorderMode]) {
        borderMode =>
          when(service.getBorderModes())
            .thenReturn(Future.successful(Seq(borderMode)))

          val userAnswers = setModeOfTransportAtTheBorderOnUserAnswersLens.set(
            None
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(BorderModeOfTransportPage) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(service.getBorderModes())
        .thenReturn(Future.failed(new RuntimeException("")))

      whenReady[Throwable, Assertion](transformer.transform.apply(emptyUserAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
