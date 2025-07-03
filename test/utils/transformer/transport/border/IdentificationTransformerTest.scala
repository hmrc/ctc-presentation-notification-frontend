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
import generated.ActiveBorderTransportMeansType03
import generators.Generators
import models.Index
import models.reference.transport.border.active.Identification
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.border.active.IdentificationPage
import services.MeansOfTransportIdentificationTypesActiveService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentificationTransformerTest extends SpecBase with Generators {
  private val service     = mock[MeansOfTransportIdentificationTypesActiveService]
  private val transformer = new IdentificationTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "IdentificationTransformer" - {

    "must skip transforming if there is no border means" in {
      val userAnswers = setBorderMeansAnswersLens.replace(
        Nil
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(IdentificationPage(Index(0))) mustBe None
    }

    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        forAll(arbitrary[ActiveBorderTransportMeansType03], arbitrary[Identification]) {
          (borderTransportMeans, identification) =>
            when(service.getMeansOfTransportIdentificationTypesActive())
              .thenReturn(Future.successful(Seq(identification)))

            val userAnswers = setBorderMeansAnswersLens.replace(
              Seq(borderTransportMeans.copy(typeOfIdentification = identification.code))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(IdentificationPage(Index(0))).value mustBe identification
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      forAll(arbitrary[ActiveBorderTransportMeansType03], arbitrary[Identification]) {
        (borderTransportMeans, identification) =>
          when(service.getMeansOfTransportIdentificationTypesActive())
            .thenReturn(Future.successful(Nil))

          val userAnswers = setBorderMeansAnswersLens.replace(
            Seq(borderTransportMeans.copy(typeOfIdentification = identification.code))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(IdentificationPage(Index(0))) mustBe None
      }
    }

    "must return failure if the service fails" in {
      forAll(arbitrary[ActiveBorderTransportMeansType03]) {
        borderTransportMeans =>
          when(service.getMeansOfTransportIdentificationTypesActive())
            .thenReturn(Future.failed(new RuntimeException("")))

          val userAnswers = setBorderMeansAnswersLens.replace(
            Seq(borderTransportMeans)
          )(emptyUserAnswers)

          whenReady[Throwable, Assertion](transformer.transform.apply(userAnswers).failed) {
            _ mustBe an[Exception]
          }
      }
    }
  }
}
