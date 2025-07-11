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
import generated.LocationOfGoodsType04
import generators.Generators
import models.reference.LocationType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.locationOfGoods.{InferredLocationTypePage, LocationTypePage}
import services.LocationTypeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationTypeTransformerTest extends SpecBase with Generators {
  private val service     = mock[LocationTypeService]
  private val transformer = new LocationTypeTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "LocationTypeTransformer" - {

    "must skip transforming if there is no location type" in {
      val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(None)(emptyUserAnswers)
      val result      = transformer.transform.apply(userAnswers).futureValue
      result mustBe userAnswers
    }

    "must return updated answers when the location type from departure data can be found in service response" - {
      "when multiple location types returned" in {
        forAll(arbitrary[LocationOfGoodsType04], arbitrary[LocationType], arbitrary[LocationType]) {
          (locationOfGoods, locationType1, locationType2) =>
            when(service.getLocationTypes(any())(any()))
              .thenReturn(Future.successful(Seq(locationType1, locationType2)))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
              Some(locationOfGoods.copy(typeOfLocation = locationType1.`type`))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(LocationTypePage) mustBe Some(locationType1)
            result.get(InferredLocationTypePage) must not be defined
        }
      }

      "when one location type returned" in {
        forAll(arbitrary[LocationOfGoodsType04], arbitrary[LocationType]) {
          (locationOfGoods, locationType) =>
            when(service.getLocationTypes(any())(any()))
              .thenReturn(Future.successful(Seq(locationType)))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
              Some(locationOfGoods.copy(typeOfLocation = locationType.`type`))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(LocationTypePage) must not be defined
            result.get(InferredLocationTypePage) mustBe Some(locationType)
        }
      }
    }
  }

  "must return None when the location type from departure data cannot be found in service response" in {
    forAll(arbitrary[LocationOfGoodsType04], arbitrary[LocationType]) {
      (locationOfGoods, locationType) =>
        when(service.getLocationTypes(any())(any()))
          .thenReturn(Future.successful(Nil))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
          Some(locationOfGoods.copy(typeOfLocation = locationType.`type`))
        )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(LocationTypePage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    forAll(arbitrary[LocationOfGoodsType04]) {
      locationOfGoods =>
        when(service.getLocationTypes(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("")))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
          Some(locationOfGoods)
        )(emptyUserAnswers)

        whenReady[Throwable, Assertion](transformer.transform.apply(userAnswers).failed) {
          _ mustBe an[Exception]
        }
    }
  }
}
