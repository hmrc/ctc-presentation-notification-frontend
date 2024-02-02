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
import base.TestMessageData.borderTransportMeans
import models.reference.CustomsOffice
import models.{Index, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.transport.border.active.CustomsOfficeActiveBorderPage
import services.CustomsOfficesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficeTransformerTest extends SpecBase {
  private val service     = mock[CustomsOfficesService]
  private val transformer = new CustomsOfficeTransformer(service)

  override def beforeEach() =
    reset(service)

  "CustomsOfficeTransformer" - {

    "must skip transforming if there is no border means" in {
      forAll(Gen.oneOf(Option(List()), None)) {
        borderMeans =>
          val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(borderMeans)(emptyUserAnswers)
          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers mustBe userAnswers
          }
      }
    }

    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        val customsOffice = CustomsOffice("GB000028", "CustomsOffice1", None)
        when(service.getCustomsOfficesByMultipleIds(Seq("GB000028"))).thenReturn(Future.successful(Seq(customsOffice)))

        val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(Option(List(borderTransportMeans)))(emptyUserAnswers)
        val index       = Index(0)
        userAnswers.get(CustomsOfficeActiveBorderPage(index)) mustBe None

        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(CustomsOfficeActiveBorderPage(index)) mustBe Some(customsOffice)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      val customsOffice = CustomsOffice("something else", "CustomsOffice2", None)
      when(service.getCustomsOfficesByMultipleIds(Seq("GB000028"))).thenReturn(Future.successful(Seq(customsOffice)))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(CustomsOfficeActiveBorderPage(index)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(CustomsOfficeActiveBorderPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(service.getCustomsOfficesByMultipleIds(Seq("GB000028"))).thenReturn(Future.failed(new RuntimeException("")))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(CustomsOfficeActiveBorderPage(index)) mustBe None

      whenReady[Throwable, Assertion](transformer.transform(hc)(userAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
