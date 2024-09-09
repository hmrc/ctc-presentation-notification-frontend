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

package utils.transformer.representative

import base.SpecBase
import generated.RepresentativeType05
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.representative.EoriPage

class RepresentativeEoriTransformerSpec extends SpecBase with Generators {
  val transformer = new RepresentativeEoriTransformer()

  "RepresentativeEoriTransformer" - {
    "must return updated answers with representative EoriPage" in {
      forAll(arbitrary[RepresentativeType05], nonEmptyString) {
        (representative, eori) =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative.copy(identificationNumber = eori))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(EoriPage).value mustBe eori
      }
    }

    "must not update if representative eori is None" in {
      val userAnswers = setRepresentativeOnUserAnswersLens.replace(
        None
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(EoriPage) mustBe None
    }
  }
}
