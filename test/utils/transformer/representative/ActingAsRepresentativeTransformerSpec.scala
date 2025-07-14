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
import generated.RepresentativeType06
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.ActingAsRepresentativePage

class ActingAsRepresentativeTransformerSpec extends SpecBase with Generators {

  val transformer = new ActingAsRepresentativeTransformer()

  "ActingAsRepresentativeTransformer" - {
    "when representative details is present must return updated answers with ActingAsRepresentative page as true" in {
      forAll(arbitrary[RepresentativeType06]) {
        representative =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative)
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(ActingAsRepresentativePage).value mustEqual true
      }
    }

    "when representative details not present must return updated answers with ActingAsRepresentative page as false" in {
      val userAnswers = setRepresentativeOnUserAnswersLens.replace(
        None
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(ActingAsRepresentativePage).value mustEqual false
    }
  }
}
