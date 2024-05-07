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
import base.TestMessageData.{locationOfGoods, messageData}
import models.SelectableList
import models.reference.CustomsOffice
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import services.CustomsOfficesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficeIdentifierTransformerTest extends SpecBase {
  private val service     = mock[CustomsOfficesService]
  private val transformer = new CustomsOfficeIdentifierTransformer(service)

  override def beforeEach() =
    reset(service)

  "CustomsOfficeIdentifierTransformer" - {

    "must skip transforming if there is no customs office data" in {
      val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(Some(locationOfGoods.copy(CustomsOffice = None)))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers mustBe userAnswers
      }
    }

    "must return updated answers when the customs office from departure data can be found in service response" in {
      val customsOfficeReference = "GB000028"
      val customsOffice          = CustomsOffice("GB000028", "CustomsOffice1", Some("+447806985236"))

      when(service.getCustomsOfficesOfDepartureForCountry(customsOfficeReference.take(2))).thenReturn(Future.successful(SelectableList(Seq(customsOffice))))

      val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
        Some(locationOfGoods.copy(CustomsOffice = Some(locationOfGoods.CustomsOffice.get.copy(referenceNumber = customsOfficeReference))))
      )(emptyUserAnswers.copy(departureData = messageData.copy(CustomsOfficeOfDeparture = "GB000011")))
      userAnswers.get(CustomsOfficeIdentifierPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(CustomsOfficeIdentifierPage) mustBe Some(customsOffice)
      }
    }
  }

  "must return None when the customs office from departure data cannot be found in service response" in {
    when(service.getCustomsOfficesOfDepartureForCountry("TR")).thenReturn(Future.successful(SelectableList(Seq())))

    val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
      Some(locationOfGoods.copy(CustomsOffice = Some(locationOfGoods.CustomsOffice.get.copy(referenceNumber = "TR000011"))))
    )(emptyUserAnswers.copy(departureData = messageData.copy(CustomsOfficeOfDeparture = "TR000011")))

    whenReady(transformer.transform(hc)(userAnswers)) {
      updatedUserAnswers =>
        updatedUserAnswers.get(CustomsOfficeIdentifierPage) mustBe None
    }
  }

  "must return failure if the service fails" in {
    val customsOfficeReference = "GB000028"
    when(service.getCustomsOfficesOfDepartureForCountry(customsOfficeReference.take(2))).thenReturn(Future.failed(new RuntimeException("")))

    val userAnswers = setLocationOfGoodsOnUserAnswersLens.set(
      Some(locationOfGoods.copy(CustomsOffice = Some(locationOfGoods.CustomsOffice.get.copy(referenceNumber = customsOfficeReference))))
    )(emptyUserAnswers.copy(departureData = messageData.copy(CustomsOfficeOfDeparture = "GB000011")))

    whenReady[Throwable, Assertion](transformer.transform(hc)(userAnswers).failed) {
      _ mustBe an[Exception]
    }
  }
}
