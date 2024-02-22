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

package utils.transformer.locationOfGoods

import base.SpecBase
import base.TestMessageData.locationOfGoods
import models.reference.{Country, CountryCode}
import models.{SelectableList, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.locationOfGoods.CountryPage
import services.CountriesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountryTransformerTest extends SpecBase {
  private val service     = mock[CountriesService]
  private val transformer = new CountryTransformer(service)

  override def beforeEach() =
    reset(service)

  "CountryTransformer" - {

    "must skip transforming if there is no country data" in {
      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(Some(locationOfGoods.copy(Address = None)))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers mustBe userAnswers
      }
    }

    "must return updated answers when the code from departure data can be found in service response" in {
      val country = Country(CountryCode("GB"), "Great Britain")
      when(service.getCountries()).thenReturn(Future.successful(SelectableList(Seq(country))))

      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(
        Some(locationOfGoods.copy(Address = Some(locationOfGoods.Address.get.copy(country = "GB"))))
      )(emptyUserAnswers)
      userAnswers.get(CountryPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(CountryPage) mustBe Some(country)
      }
    }
  }

  "must return None when the code from departure data cannot be found in service response" in {
    val country = Country(CountryCode("GB"), "Great Britain")
    when(service.getCountries()).thenReturn(Future.successful(SelectableList(Seq(country))))

    val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(
      Some(locationOfGoods.copy(Address = Some(locationOfGoods.Address.get.copy(country = "FR"))))
    )(emptyUserAnswers)

    whenReady(transformer.transform(hc)(userAnswers)) {
      updatedUserAnswers =>
        updatedUserAnswers.get(CountryPage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    when(service.getCountries()).thenReturn(Future.failed(new RuntimeException("")))

    whenReady[Throwable, Assertion](transformer.transform(hc)(emptyUserAnswers).failed) {
      _ mustBe an[Exception]
    }
  }
}
