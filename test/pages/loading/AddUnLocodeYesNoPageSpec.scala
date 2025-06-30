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

class AddUnLocodeYesNoPageSpec extends PageBehaviours {

  "AddUnLocodeYesNoPage" - {

    beRetrievable[Boolean](AddUnLocodeYesNoPage)

    beSettable[Boolean](AddUnLocodeYesNoPage)

    beRemovable[Boolean](AddUnLocodeYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove unlocode, addExtraInformation, country and location pages in 15/13/170" in {
          forAll(arbitraryUnLocode.arbitrary, arbitraryCountry.arbitrary, nonEmptyString.sample.value) {
            (unlocode, country, location) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
                .setValue(UnLocodePage, unlocode.unLocodeExtendedCode)
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val result = userAnswers.setValue(AddUnLocodeYesNoPage, false)

              result.get(UnLocodePage) must not be defined
              result.get(AddExtraInformationYesNoPage) must not be defined
              result.get(CountryPage) must not be defined
              result.get(LocationPage) must not be defined
          }
        }
      }
      "when yes selected" - {
        "must remove unlocode, addExtraInformation, country and location pages in 15/13/170" in {
          forAll(arbitraryUnLocode.arbitrary, arbitraryCountry.arbitrary, nonEmptyString.sample.value) {
            (unlocode, country, location) =>
              val userAnswers = emptyUserAnswers
                .setValue(UnLocodePage, unlocode.unLocodeExtendedCode)
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val result = userAnswers.setValue(AddUnLocodeYesNoPage, true)

              result.get(UnLocodePage) must not be defined
              result.get(AddExtraInformationYesNoPage) must not be defined
              result.get(CountryPage) must not be defined
              result.get(LocationPage) must not be defined
          }
        }
      }
    }
  }
}
