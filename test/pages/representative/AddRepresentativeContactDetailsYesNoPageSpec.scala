/*
 * Copyright 2023 HM Revenue & Customs
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

package pages.representative

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddRepresentativeContactDetailsYesNoPageSpec extends PageBehaviours {

  "AddRepresentativeYesNoPage" - {

    beRetrievable[Boolean](AddRepresentativeContactDetailsYesNoPage)

    beSettable[Boolean](AddRepresentativeContactDetailsYesNoPage)

    beRemovable[Boolean](AddRepresentativeContactDetailsYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up name and telephone pages" in {
          forAll(arbitrary[String], arbitrary[String]) {
            (name, telephone) =>
              val preChange = emptyUserAnswers
                .setValue(AddRepresentativeContactDetailsYesNoPage, true)
                .setValue(NamePage, name)
                .setValue(RepresentativePhoneNumberPage, telephone)
              val postChange = preChange.setValue(AddRepresentativeContactDetailsYesNoPage, false)

              postChange.get(NamePage) mustNot be(defined)
              postChange.get(RepresentativePhoneNumberPage) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(arbitrary[String], arbitrary[String]) {
            (name, telephone) =>
              val preChange = emptyUserAnswers
                .setValue(NamePage, name)
                .setValue(RepresentativePhoneNumberPage, telephone)
              val postChange = preChange.setValue(AddRepresentativeContactDetailsYesNoPage, true)

              postChange.get(NamePage) must be(defined)
              postChange.get(RepresentativePhoneNumberPage) must be(defined)
          }
        }
      }
    }
  }
}
