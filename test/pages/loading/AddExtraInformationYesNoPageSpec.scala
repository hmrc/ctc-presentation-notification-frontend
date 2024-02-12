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

package pages.loading

import pages.behaviours.PageBehaviours

class AddExtraInformationYesNoPageSpec extends PageBehaviours {

  "AddExtraInformationYesNoPage" - {

    beRetrievable[Boolean](AddExtraInformationYesNoPage)

    beSettable[Boolean](AddExtraInformationYesNoPage)

    beRemovable[Boolean](AddExtraInformationYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove country and location pages in 15/13/170" in {
          forAll(arbitraryCountry.arbitrary, nonEmptyString.sample.value) {
            (country, location) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val result = userAnswers.setValue(AddExtraInformationYesNoPage, false)

              result.get(CountryPage) must not be defined
              result.get(LocationPage) must not be defined

          }
        }
      }
      "when yes selected" - {
        "must remove country and location pages in 15/13/170" in {
          forAll(arbitraryCountry.arbitrary, nonEmptyString.sample.value) {
            (country, location) =>
              val userAnswers = emptyUserAnswers
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val result = userAnswers.setValue(AddExtraInformationYesNoPage, true)

              result.get(CountryPage) must not be defined
              result.get(LocationPage) must not be defined

          }
        }
      }
    }
  }
}
