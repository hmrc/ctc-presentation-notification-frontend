/*
 * Copyright 2025 HM Revenue & Customs
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
import generated.ContactPersonType03
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.representative.{AddRepresentativeContactDetailsYesNoPage, NamePage, RepresentativePhoneNumberPage}

class ContactPersonTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ContactPersonTransformer]

  "must transform data " - {
    "when contactPerson defined" in {
      forAll(
        arbitrary[ContactPersonType03]
      ) {
        contactPerson =>
          val result = transformer.transform(Some(contactPerson)).apply(emptyUserAnswers).futureValue

          result.getValue(NamePage) mustEqual contactPerson.name
          result.getValue(AddRepresentativeContactDetailsYesNoPage) mustEqual true
          result.getValue(RepresentativePhoneNumberPage) mustEqual contactPerson.phoneNumber
      }
    }
    "when contactPerson undefined" in {
      val result = transformer.transform(None)(hc).apply(emptyUserAnswers).futureValue
      result.get(NamePage) must not be defined
      result.getValue(AddRepresentativeContactDetailsYesNoPage) mustEqual false
      result.get(RepresentativePhoneNumberPage) must not be defined
    }
  }

}
