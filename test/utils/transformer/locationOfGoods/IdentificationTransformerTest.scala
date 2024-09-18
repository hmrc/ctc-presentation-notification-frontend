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
import generated.LocationOfGoodsType05
import generators.Generators
import models.LocationOfGoodsIdentification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.locationOfGoods.{IdentificationPage, InferredIdentificationPage}
import services.LocationOfGoodsIdentificationTypeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentificationTransformerTest extends SpecBase with Generators {
  private val service     = mock[LocationOfGoodsIdentificationTypeService]
  private val transformer = new IdentificationTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "IdentificationTransformer" - {

    "must skip transforming if there is no qualifier identification" in {
      val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(None)(emptyUserAnswers)
      val result      = transformer.transform.apply(userAnswers).futureValue
      result mustBe userAnswers
    }

    "must return updated answers when the identification from departure data can be found in service response" - {
      "when multiple identifier types returned" in {
        forAll(arbitrary[LocationOfGoodsType05], arbitrary[LocationOfGoodsIdentification], arbitrary[LocationOfGoodsIdentification]) {
          (locationOfGoods, identification1, identification2) =>
            when(service.getLocationOfGoodsIdentificationTypes(any())(any()))
              .thenReturn(Future.successful(Seq(identification1, identification2)))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(qualifierOfIdentification = identification1.qualifier))
              )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(IdentificationPage) mustBe Some(identification1)
            result.get(InferredIdentificationPage) must not be defined
        }
      }

      "when one identifier type returned" in {
        forAll(arbitrary[LocationOfGoodsType05], arbitrary[LocationOfGoodsIdentification]) {
          (locationOfGoods, identification) =>
            when(service.getLocationOfGoodsIdentificationTypes(any())(any()))
              .thenReturn(Future.successful(Seq(identification)))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(qualifierOfIdentification = identification.qualifier))
              )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(IdentificationPage) must not be defined
            result.get(InferredIdentificationPage) mustBe Some(identification)
        }
      }
    }
  }

  "must return None when the identification from departure data cannot be found in service response" in {
    forAll(arbitrary[LocationOfGoodsType05], arbitrary[LocationOfGoodsIdentification]) {
      (locationOfGoods, identification) =>
        when(service.getLocationOfGoodsIdentificationTypes(any())(any()))
          .thenReturn(Future.successful(Seq()))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens
          .replace(
            Some(locationOfGoods.copy(qualifierOfIdentification = identification.qualifier))
          )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(IdentificationPage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    forAll(arbitrary[LocationOfGoodsType05]) {
      locationOfGoods =>
        when(service.getLocationOfGoodsIdentificationTypes(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("")))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens
          .replace(
            Some(locationOfGoods)
          )(emptyUserAnswers)

        whenReady[Throwable, Assertion](transformer.transform.apply(userAnswers).failed) {
          _ mustBe an[Exception]
        }
    }
  }
}
