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
import base.TestMessageData.messageData.isSimplified
import models.{LocationType, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.locationOfGoods.LocationTypePage
import services.LocationTypeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationTypeTransformerTest extends SpecBase {
  private val service     = mock[LocationTypeService]
  private val transformer = new LocationTypeTransformer(service)

  override def beforeEach() =
    reset(service)

  "LocationTypeTransformer" - {

    "must skip transforming if there is no location type" in {
      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(None)(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers mustBe userAnswers
      }
    }

    "must return updated answers when the location type from departure data can be found in service response" in {
      val locationType = LocationType(locationOfGoods.typeOfLocation, "description")
      when(service.getLocationTypes(isSimplified)).thenReturn(Future.successful(Seq(locationType)))

      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(
        Some(locationOfGoods.copy(Address = Some(locationOfGoods.Address.get.copy(country = "GB"))))
      )(emptyUserAnswers)
      userAnswers.get(LocationTypePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(LocationTypePage) mustBe Some(locationType)
      }
    }
  }

  "must return None when the location type from departure data cannot be found in service response" in {
    val locationType = LocationType(locationOfGoods.typeOfLocation, "description")
    when(service.getLocationTypes(isSimplified)).thenReturn(Future.successful(Seq(locationType)))

    val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(
      Some(locationOfGoods.copy(typeOfLocation = "SomethingElse"))
    )(emptyUserAnswers)

    whenReady(transformer.transform(hc)(userAnswers)) {
      updatedUserAnswers =>
        updatedUserAnswers.get(LocationTypePage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    when(service.getLocationTypes(isSimplified)).thenReturn(Future.failed(new RuntimeException("")))

    whenReady[Throwable, Assertion](transformer.transform(hc)(emptyUserAnswers).failed) {
      _ mustBe an[Exception]
    }
  }
}
