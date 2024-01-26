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
import models.reference.Nationality
import models.{Index, SelectableList, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.transport.border.active.NationalityPage
import services.NationalitiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NationalityTransformerTest extends SpecBase {
  private val service     = mock[NationalitiesService]
  private val transformer = new NationalityTransformer(service)

  override def beforeEach() =
    reset(service)

  "NationalityTransformer" - {

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
        val nationality = Nationality("FR", "France")
        when(service.getNationalities()).thenReturn(Future.successful(SelectableList(Seq(nationality))))

        val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(Option(List(borderTransportMeans)))(emptyUserAnswers)
        val index       = Index(0)
        userAnswers.get(NationalityPage(index)) mustBe None

        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(NationalityPage(index)) mustBe Some(nationality)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      val nationality = Nationality("TR", "Türkiye")
      when(service.getNationalities()).thenReturn(Future.successful(SelectableList(Seq(nationality))))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(NationalityPage(index)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(NationalityPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(service.getNationalities()).thenReturn(Future.failed(new RuntimeException("")))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(NationalityPage(index)) mustBe None

      whenReady[Throwable, Assertion](transformer.transform(hc)(userAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
