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
import generated._
import generators.Generators
import models.SelectableList
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import services.CustomsOfficesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficeIdentifierTransformerTest extends SpecBase with Generators {
  private val service     = mock[CustomsOfficesService]
  private val transformer = new CustomsOfficeIdentifierTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "CustomsOfficeIdentifierTransformer" - {

    "must skip transforming if there is no customs office data" in {
      forAll(arbitrary[LocationOfGoodsType05]) {
        locationOfGoods =>
          val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
            Some(locationOfGoods.copy(CustomsOffice = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result mustBe userAnswers
      }
    }

    "must return updated answers when the customs office from departure data can be found in service response" in {
      forAll(arbitrary[LocationOfGoodsType05], arbitrary[CustomsOffice]) {
        (locationOfGoods, customsOffice) =>
          when(service.getCustomsOfficesOfDepartureForCountry(any())(any()))
            .thenReturn(Future.successful(SelectableList(Seq(customsOffice))))

          val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
            Some(locationOfGoods.copy(CustomsOffice = Some(CustomsOfficeType02(customsOffice.id))))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(CustomsOfficeIdentifierPage) mustBe Some(customsOffice)
      }
    }
  }

  "must return None when the customs office from departure data cannot be found in service response" in {
    forAll(arbitrary[LocationOfGoodsType05], arbitrary[CustomsOffice]) {
      (locationOfGoods, customsOffice) =>
        when(service.getCustomsOfficesOfDepartureForCountry(any())(any()))
          .thenReturn(Future.successful(SelectableList(Seq())))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
          Some(locationOfGoods.copy(CustomsOffice = Some(CustomsOfficeType02(customsOffice.id))))
        )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(CustomsOfficeIdentifierPage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    forAll(arbitrary[LocationOfGoodsType05], arbitrary[CustomsOffice]) {
      (locationOfGoods, customsOffice) =>
        when(service.getCustomsOfficesOfDepartureForCountry(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("")))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
          Some(locationOfGoods.copy(CustomsOffice = Some(CustomsOfficeType02(customsOffice.id))))
        )(emptyUserAnswers)

        whenReady[Throwable, Assertion](transformer.transform.apply(userAnswers).failed) {
          _ mustBe an[Exception]
        }
    }
  }
}
