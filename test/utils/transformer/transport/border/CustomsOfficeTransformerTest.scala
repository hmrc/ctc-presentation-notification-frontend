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
import generated.ActiveBorderTransportMeansType02
import generators.Generators
import models.Index
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.border.active.CustomsOfficeActiveBorderPage
import services.CustomsOfficesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficeTransformerTest extends SpecBase with Generators {
  private val service     = mock[CustomsOfficesService]
  private val transformer = new CustomsOfficeTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "CustomsOfficeTransformer" - {

    "must skip transforming if there is no border means" in {
      val userAnswers = setBorderMeansAnswersLens.replace(
        Nil
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(CustomsOfficeActiveBorderPage(Index(0))) mustBe None
    }

    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        forAll(arbitrary[ActiveBorderTransportMeansType02], arbitrary[CustomsOffice]) {
          (borderTransportMeans, customsOffice) =>
            when(service.getCustomsOfficesByMultipleIds(any())(any()))
              .thenReturn(Future.successful(Seq(customsOffice)))

            val userAnswers = setBorderMeansAnswersLens.replace(
              Seq(borderTransportMeans.copy(customsOfficeAtBorderReferenceNumber = Some(customsOffice.id)))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(CustomsOfficeActiveBorderPage(index)) mustBe Some(customsOffice)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02], arbitrary[CustomsOffice]) {
        (borderTransportMeans, customsOffice) =>
          when(service.getCustomsOfficesByMultipleIds(any())(any()))
            .thenReturn(Future.successful(Nil))

          val userAnswers = setBorderMeansAnswersLens.replace(
            Seq(borderTransportMeans.copy(customsOfficeAtBorderReferenceNumber = Some(customsOffice.id)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(CustomsOfficeActiveBorderPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02]) {
        borderTransportMeans =>
          when(service.getCustomsOfficesByMultipleIds(any())(any()))
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
