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

package pages.transport.border

import pages.behaviours.PageBehaviours
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.border.active._

class AddBorderMeansOfTransportYesNoPageSpec extends PageBehaviours {

  "AddBorderMeansOfTransportYesNoPage" - {

    beRetrievable[Boolean](AddBorderMeansOfTransportYesNoPage)
    beSettable[Boolean](AddBorderMeansOfTransportYesNoPage)
    beRemovable[Boolean](AddBorderMeansOfTransportYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove ActiveBorderList in ie170" in {
          forAll(arbitraryIdentificationActive.arbitrary, nonEmptyString, arbitraryNationality.arbitrary, arbitraryCustomsOffice.arbitrary, nonEmptyString) {
            (identification, identificationNumber, nationality, customsOffice, conveyanceRefNumber) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddBorderMeansOfTransportYesNoPage, true)
                .setValue(IdentificationPage(activeIndex), identification)
                .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                .setValue(NationalityPage(activeIndex), nationality)
                .setValue(CustomsOfficeActiveBorderPage(activeIndex), customsOffice)
                .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)

              val result = userAnswers.setValue(AddBorderMeansOfTransportYesNoPage, false)

              result.get(BorderActiveListSection) must not be defined

          }
        }
      }
    }
  }
}
