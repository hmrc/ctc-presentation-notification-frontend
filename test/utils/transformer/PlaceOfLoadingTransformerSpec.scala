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
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class PlaceOfLoadingTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[PlaceOfLoadingTransformer]

  "must transform data" - {
    import pages.loading.*

    "when values defined" in {
      forAll(
        arbitrary[PlaceOfLoadingType].map(
          _.copy(
            UNLocode = Some("UNLocode"),
            country = Some("UK")
          )
        )
      ) {
        placeOfLoading =>
          val result = transformer.transform(Some(placeOfLoading)).apply(emptyUserAnswers).futureValue

          result.getValue(AddUnLocodeYesNoPage) mustEqual true
          result.get(UnLocodePage) mustEqual placeOfLoading.UNLocode
          result.get(LocationPage) mustEqual placeOfLoading.location
          result.getValue(AddExtraInformationYesNoPage) mustEqual true
      }
    }
  }
}
