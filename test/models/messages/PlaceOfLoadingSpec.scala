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

package models.messages

import base.SpecBase
import generators.Generators
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading._
import play.api.libs.json.JsError

class PlaceOfLoadingSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Place of loading" - {

    "must read from IE170 user answers" - {
      "when UN/LOCODE is defined" in {
        forAll(Gen.alphaNumStr) {
          unLocode =>
            val userAnswers = emptyUserAnswers.setValue(UnLocodePage, unLocode)
            val result      = userAnswers.data.as[PlaceOfLoading]
            result mustBe PlaceOfLoading(Some(unLocode), None, None)
        }
      }

      "when Country and Location are defined" in {
        forAll(arbitrary[Country], Gen.alphaNumStr) {
          (country, location) =>
            val userAnswers = emptyUserAnswers
              .setValue(CountryPage, country)
              .setValue(LocationPage, location)
            val result = userAnswers.data.as[PlaceOfLoading]
            result mustBe PlaceOfLoading(None, Some(country.code.code), Some(location))
        }
      }
    }

    "must not read from IE170 user answers" - {
      "when all fields are undefined" in {
        val result = emptyUserAnswers.data.validate[PlaceOfLoading]
        result mustBe a[JsError]
      }
    }
  }

}
