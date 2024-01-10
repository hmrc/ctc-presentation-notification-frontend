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

package pages.transport

import pages.behaviours.PageBehaviours

class AddInlandModeOfTransportYesNoPageSpec extends PageBehaviours {

  "AddInlandModeOfTransportYesNoPage" - {

    beRetrievable[Boolean](AddInlandModeOfTransportYesNoPage)
    beSettable[Boolean](AddInlandModeOfTransportYesNoPage)
    beRemovable[Boolean](AddInlandModeOfTransportYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove country and location pages in 15/13/170" in {
          forAll(arbitraryInlandModeOfTransport.arbitrary) {
            inlandMode =>
              val userAnswers = emptyUserAnswers
                .setValue(AddInlandModeOfTransportYesNoPage, true)
                .setValue(InlandModePage, inlandMode)

              val result = userAnswers.setValue(AddInlandModeOfTransportYesNoPage, false)

              result.get(InlandModePage) must not be defined
              result.departureData.Consignment.inlandModeOfTransport must not be defined
          }
        }
      }
    }

  }
}
