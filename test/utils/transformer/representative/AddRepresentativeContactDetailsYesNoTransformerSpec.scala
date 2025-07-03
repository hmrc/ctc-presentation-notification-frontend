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
import generated._
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.representative.AddRepresentativeContactDetailsYesNoPage

class AddRepresentativeContactDetailsYesNoTransformerSpec extends SpecBase with Generators {

  val transformer = new AddRepresentativeContactDetailsYesNoTransformer()

  "AddRepresentativeContactDetailsYesNoTransformer" - {
    "when representative contact details is present must return updated answers with AddRepresentativeContactDetailsYesNoPage as true" in {
      forAll(arbitrary[RepresentativeType06], arbitrary[ContactPersonType03]) {
        (representative, contactPerson) =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative.copy(ContactPerson = Some(contactPerson)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddRepresentativeContactDetailsYesNoPage) mustBe Some(true)
      }
    }

    "when representative contact details not present must return updated answers with AddRepresentativeContactDetailsYesNoPage as false" in {
      forAll(arbitrary[RepresentativeType06]) {
        representative =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative.copy(ContactPerson = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddRepresentativeContactDetailsYesNoPage) mustBe Some(false)
      }
    }
  }
}
