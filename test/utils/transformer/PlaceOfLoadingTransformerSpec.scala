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

package utils.transformer

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.PlaceOfLoadingType
import generators.Generators
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.CountriesService

import scala.concurrent.Future

class PlaceOfLoadingTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[PlaceOfLoadingTransformer]

  private lazy val mockCountryService = mock[CountriesService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CountriesService].toInstance(mockCountryService)
      )

  "must transform data" - {

    "when UNLocode is undefined and Country is defined" in {
      val country = Country(CountryCode("GB"), "value")
      forAll(
        arbitrary[PlaceOfLoadingType].map(
          _.copy(
            UNLocode = None,
            country = Some("GB"),
            location = Some("Paris")
          )
        )
      ) {
        placeOfLoading =>
          when(mockCountryService.getCountry(any())(any()))
            .thenReturn(Future.successful(country))
          val result = transformer.transform(Some(placeOfLoading)).apply(emptyUserAnswers).futureValue

          result.get(AddUnLocodeYesNoPage).value mustEqual false
          result.get(UnLocodePage) must not be defined
          result.get(AddExtraInformationYesNoPage).value mustEqual false
          result.getValue(CountryPage) mustEqual country
          result.get(LocationPage) mustEqual placeOfLoading.location
      }
    }
    "when UNLocode is defined but Country is undefined " in {
      val country = Country(CountryCode("GB"), "value")
      forAll(
        arbitrary[PlaceOfLoadingType].map(
          _.copy(
            UNLocode = Some("value"),
            country = None,
            location = Some("Paris")
          )
        )
      ) {
        placeOfLoading =>
          when(mockCountryService.getCountry(any())(any()))
            .thenReturn(Future.successful(country))
          val result = transformer.transform(Some(placeOfLoading)).apply(emptyUserAnswers).futureValue

          result.get(AddUnLocodeYesNoPage).value mustEqual true
          result.get(UnLocodePage) mustEqual placeOfLoading.UNLocode
          result.get(AddExtraInformationYesNoPage).value mustEqual false
          result.get(CountryPage) must not be defined
          result.get(LocationPage) mustEqual placeOfLoading.location
      }
    }
    "when all values are defined " in {
      val country = Country(CountryCode("GB"), "value")
      forAll(
        arbitrary[PlaceOfLoadingType].map(
          _.copy(
            UNLocode = Some("value"),
            country = Some("GB"),
            location = Some("Paris")
          )
        )
      ) {
        placeOfLoading =>
          when(mockCountryService.getCountry(any())(any()))
            .thenReturn(Future.successful(country))
          val result = transformer.transform(Some(placeOfLoading)).apply(emptyUserAnswers).futureValue

          result.get(AddUnLocodeYesNoPage).value mustEqual true
          result.get(UnLocodePage) mustEqual placeOfLoading.UNLocode
          result.get(AddExtraInformationYesNoPage).value mustEqual true
          result.getValue(CountryPage) mustEqual country
          result.get(LocationPage) mustEqual placeOfLoading.location
      }
    }
  }
}
