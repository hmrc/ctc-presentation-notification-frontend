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

package utils.transformer.transport.placeOfLoading

import base.SpecBase
import generated.PlaceOfLoadingType
import generators.Generators
import models.SelectableList
import models.reference.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.loading.CountryPage
import services.CountriesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountryTransformerTest extends SpecBase with Generators {

  private val service     = mock[CountriesService]
  private val transformer = new CountryTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "CountryTransformer" - {
    "must return updated answers with CountryPage" in {
      forAll(arbitrary[PlaceOfLoadingType], arbitrary[Country]) {
        (placeOfLoading, country) =>
          when(service.getCountries()(any()))
            .thenReturn(Future.successful(SelectableList(Seq(country))))

          val userAnswers = setPlaceOfLoadingOnUserAnswersLens.replace(
            Some(placeOfLoading.copy(country = Some(country.code.code)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(CountryPage).value mustEqual country
      }
    }
  }
}
