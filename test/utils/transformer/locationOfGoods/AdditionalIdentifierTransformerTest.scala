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
import generated.LocationOfGoodsType05
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.locationOfGoods.AdditionalIdentifierPage

class AdditionalIdentifierTransformerTest extends SpecBase with Generators {
  val transformer = new AdditionalIdentifierTransformer()

  "AdditionalIdentifierTransformer" - {

    "must return updated answers with AdditionalIdentifierPage" in {
      forAll(arbitrary[LocationOfGoodsType05], nonEmptyString) {
        (locationOfGoods, additionalIdentifier) =>
          val userAnswers = setLocationOfGoodsOnUserAnswersLens
            .set(
              Option(locationOfGoods.copy(additionalIdentifier = Some(additionalIdentifier)))
            )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AdditionalIdentifierPage).value mustBe additionalIdentifier
      }
    }
  }
}
