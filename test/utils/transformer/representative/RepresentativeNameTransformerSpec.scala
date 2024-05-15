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
import generated.{ContactPersonType05, RepresentativeType05}
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.representative.NamePage

class RepresentativeNameTransformerSpec extends SpecBase with Generators {
  val transformer = new RepresentativeNameTransformer()

  "RepresentativeNameTransformer" - {
    "must return updated answers with representative NamePage" in {
      forAll(arbitrary[RepresentativeType05], arbitrary[ContactPersonType05], nonEmptyString) {
        (representative, contactPerson, name) =>
          val userAnswers = setRepresentativeOnUserAnswersLens.set(
            Some(representative.copy(ContactPerson = Some(contactPerson.copy(name = name))))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(NamePage).value mustBe name
      }
    }

    "must not update if representative name is None" in {
      forAll(arbitrary[RepresentativeType05]) {
        representative =>
          val userAnswers = setRepresentativeOnUserAnswersLens.set(
            Some(representative.copy(ContactPerson = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(NamePage) mustBe None
      }
    }
  }
}
