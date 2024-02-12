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
import generators.Generators
import models.SelectableList
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.loading.CountryPage
import services.CountriesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountryTransformerTest extends SpecBase with Generators {
  private val country1    = Country(CountryCode("GB"), "Great Britain")
  private val country2    = Country(CountryCode("FR"), "France")
  private val countryList = SelectableList(Seq(country1, country2))
  private val service     = mock[CountriesService]
  private val transformer = new CountryTransformer(service)

  override def beforeEach(): Unit =
    reset(service)

  "CountryTransformer" - {
    "must return updated answers with CountryPage" in {

      when(service.getCountries()(any())).thenReturn(Future.successful(countryList))
      val userAnswers = emptyUserAnswers
      userAnswers.get(CountryPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(CountryPage) mustBe Some(country1)
      }

    }
  }
}
