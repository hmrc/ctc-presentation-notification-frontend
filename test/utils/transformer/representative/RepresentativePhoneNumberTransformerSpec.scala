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
import generated.{ContactPersonType03, RepresentativeType06}
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.representative.RepresentativePhoneNumberPage

class RepresentativePhoneNumberTransformerSpec extends SpecBase with Generators {
  val transformer = new RepresentativePhoneNumberTransformer()

  "RepresentativePhoneNumberTransformer" - {
    "must return updated answers with RepresentativePhoneNumberPage" in {
      forAll(arbitrary[RepresentativeType06], arbitrary[ContactPersonType03], nonEmptyString) {
        (representative, contactPerson, phoneNumber) =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative.copy(ContactPerson = Some(contactPerson.copy(phoneNumber = phoneNumber))))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(RepresentativePhoneNumberPage).value mustEqual phoneNumber
      }
    }

    "must not update if representative phone number is None" in {
      forAll(arbitrary[RepresentativeType06]) {
        representative =>
          val userAnswers = setRepresentativeOnUserAnswersLens.replace(
            Some(representative.copy(ContactPerson = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(RepresentativePhoneNumberPage) must not be defined
      }
    }
  }
}
