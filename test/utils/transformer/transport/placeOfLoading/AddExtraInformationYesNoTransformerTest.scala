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
import org.scalacheck.Arbitrary.arbitrary
import pages.loading.AddExtraInformationYesNoPage

class AddExtraInformationYesNoTransformerTest extends SpecBase with Generators {
  val transformer = new AddExtraInformationYesNoTransformer

  "AddExtraInformationYesNoTransformer" - {
    "must return updated answers with AddExtraInformationYesNo" in {
      forAll(arbitrary[PlaceOfLoadingType], nonEmptyString) {
        (placeOfLoading, country) =>
          val userAnswers = setPlaceOfLoadingOnUserAnswersLens.replace(
            Some(placeOfLoading.copy(country = Some(country)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddExtraInformationYesNoPage).value mustEqual true
      }
    }
  }
}
