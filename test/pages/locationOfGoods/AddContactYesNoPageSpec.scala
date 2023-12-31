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

package pages.locationOfGoods

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}

class AddContactYesNoPageSpec extends PageBehaviours {

  "AddContactLocationOfGoodsPage" - {

    beRetrievable[Boolean](AddContactYesNoPage)

    beSettable[Boolean](AddContactYesNoPage)

    beRemovable[Boolean](AddContactYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up LocationOfGoodsContactSection" in {
          forAll(Arbitrary.arbitrary[String]) {
            str =>
              val preChange = emptyUserAnswers
                .setValue(NamePage, str)
                .setValue(PhoneNumberPage, str)

              val postChange = preChange.setValue(AddContactYesNoPage, false)

              postChange.get(NamePage) mustNot be(defined)
              postChange.get(PhoneNumberPage) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(Arbitrary.arbitrary[String]) {
            str =>
              val preChange = emptyUserAnswers
                .setValue(NamePage, str)
                .setValue(PhoneNumberPage, str)

              val postChange = preChange.setValue(AddContactYesNoPage, true)

              postChange.get(NamePage) must be(defined)
              postChange.get(PhoneNumberPage) must be(defined)
          }
        }
      }
    }

  }

}
