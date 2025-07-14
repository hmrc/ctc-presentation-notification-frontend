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
import generated.{AddressType06, LocationOfGoodsType04}
import generators.Generators
import models.SelectableList
import models.reference.Country
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.locationOfGoods.CountryPage
import services.CountriesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountryTransformerTest extends SpecBase with Generators {
  private val service     = mock[CountriesService]
  private val transformer = new CountryTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "CountryTransformer" - {

    "must skip transforming if there is no country data" in {
      forAll(arbitrary[LocationOfGoodsType04]) {
        locationOfGoods =>
          val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
            Some(locationOfGoods.copy(Address = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result mustEqual userAnswers
      }
    }

    "must return updated answers when the code from departure data can be found in service response" in {
      forAll(arbitrary[LocationOfGoodsType04], arbitrary[AddressType06], arbitrary[Country]) {
        (locationOfGoods, address, country) =>
          when(service.getCountries())
            .thenReturn(Future.successful(SelectableList(Seq(country))))

          val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
            Some(locationOfGoods.copy(Address = Some(address.copy(country = country.code.code))))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(CountryPage).value mustEqual country
      }
    }
  }

  "must return None when the code from departure data cannot be found in service response" in {
    forAll(arbitrary[LocationOfGoodsType04], arbitrary[AddressType06], arbitrary[Country]) {
      (locationOfGoods, address, country) =>
        when(service.getCountries())
          .thenReturn(Future.successful(SelectableList(Nil)))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
          Some(locationOfGoods.copy(Address = Some(address.copy(country = country.code.code))))
        )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(CountryPage) must not be defined
    }
  }

  "must return failure if the service fails" in {
    forAll(arbitrary[LocationOfGoodsType04], arbitrary[AddressType06]) {
      (locationOfGoods, address) =>
        when(service.getCountries())
          .thenReturn(Future.failed(new RuntimeException("")))

        val userAnswers = setLocationOfGoodsOnUserAnswersLens.replace(
          Some(locationOfGoods.copy(Address = Some(address)))
        )(emptyUserAnswers)

        whenReady[Throwable, Assertion](transformer.transform.apply(userAnswers).failed) {
          _ mustBe an[Exception]
        }
    }
  }
}
