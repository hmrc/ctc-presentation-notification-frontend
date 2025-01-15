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
import generated.{LocationOfGoodsType05, PostcodeAddressType02}
import generators.Generators
import models.reference.{Country, CountryCode}
import models.{PostalCodeAddress, SelectableList}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.locationOfGoods.PostalCodePage
import services.CountriesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostalCodeTransformerTest extends SpecBase with Generators {
  val countryService = mock[CountriesService]
  val transformer    = new PostalCodeTransformer(countryService)

  "PostalCodeTransformer" - {

    "must return updated answers with PostalCodePage" - {
      "when house number is defined" in {
        forAll(arbitrary[LocationOfGoodsType05], arbitrary[PostcodeAddressType02], nonEmptyString) {
          (locationOfGoods, postcodeAddress, houseNumber) =>
            val country = Country(CountryCode(postcodeAddress.country), "description")

            when(countryService.getCountries()).thenReturn(Future.successful(SelectableList(Seq(country))))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Option(locationOfGoods.copy(PostcodeAddress = Some(postcodeAddress.copy(houseNumber = Some(houseNumber)))))
              )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(PostalCodePage).value mustBe
              PostalCodeAddress(
                streetNumber = houseNumber,
                postalCode = postcodeAddress.postcode,
                country = country
              )
        }
      }

      "when house number is undefined" in {
        forAll(arbitrary[LocationOfGoodsType05], arbitrary[PostcodeAddressType02]) {
          (locationOfGoods, postcodeAddress) =>
            val country = Country(CountryCode(postcodeAddress.country), "description")

            when(countryService.getCountries()).thenReturn(Future.successful(SelectableList(Seq(country))))

            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Option(locationOfGoods.copy(PostcodeAddress = Some(postcodeAddress.copy(houseNumber = None))))
              )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(PostalCodePage).value mustBe
              PostalCodeAddress(
                streetNumber = "NA",
                postalCode = postcodeAddress.postcode,
                country = country
              )
        }
      }
    }

    "must return none if country code not found" in {
      forAll(arbitrary[LocationOfGoodsType05], arbitrary[PostcodeAddressType02]) {
        (locationOfGoods, postcodeAddress) =>
          when(countryService.getCountries()).thenReturn(Future.successful(SelectableList(Seq())))
          val userAnswers = setLocationOfGoodsOnUserAnswersLens
            .replace(
              Option(locationOfGoods.copy(PostcodeAddress = Some(postcodeAddress)))
            )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(PostalCodePage) mustBe None
      }
    }
  }
}
