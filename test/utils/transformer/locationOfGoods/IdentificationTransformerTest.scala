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
import models.{LocationOfGoodsIdentification, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.locationOfGoods.IdentificationPage
import services.LocationOfGoodsIdentificationTypeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentificationTransformerTest extends SpecBase {
  private val service     = mock[LocationOfGoodsIdentificationTypeService]
  private val transformer = new IdentificationTransformer(service)

  override def beforeEach() =
    reset(service)

  "IdentificationTransformer" - {

    "must skip transforming if there is no qualifier identification" in {
      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(None)(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers mustBe userAnswers
      }
    }

    "must return updated answers when the identification from departure data can be found in service response" in {
      val identification = LocationOfGoodsIdentification(locationOfGoods.qualifierOfIdentification, "description")
      when(service.getLocationOfGoodsIdentificationTypes(locationOfGoods.typeOfLocation)).thenReturn(Future.successful(Seq(identification)))

      val userAnswers = emptyUserAnswers
      userAnswers.get(IdentificationPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(IdentificationPage) mustBe Some(identification)
      }
    }
  }

  "must return None when the identification from departure data cannot be found in service response" in {
    when(service.getLocationOfGoodsIdentificationTypes(locationOfGoods.typeOfLocation)).thenReturn(Future.successful(Seq()))

    val userAnswers = emptyUserAnswers
    userAnswers.get(IdentificationPage) mustBe None

    whenReady(transformer.transform(hc)(userAnswers)) {
      updatedUserAnswers =>
        updatedUserAnswers.get(IdentificationPage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    when(service.getLocationOfGoodsIdentificationTypes(locationOfGoods.typeOfLocation)).thenReturn(Future.failed(new RuntimeException("")))

    whenReady[Throwable, Assertion](transformer.transform(hc)(emptyUserAnswers).failed) {
      _ mustBe an[Exception]
    }
  }
}
