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

package pages.transport.border.active

import pages.behaviours.PageBehaviours

class AddConveyanceReferenceYesNoPageSpec extends PageBehaviours {

  "AddConveyanceReferenceYesNoPage" - {

    beRetrievable[Boolean](AddConveyanceReferenceYesNoPage(index))

    beSettable[Boolean](AddConveyanceReferenceYesNoPage(index))

    beRemovable[Boolean](AddConveyanceReferenceYesNoPage(index))

    "cleanup" - {
      "when no selected" - {
        "must remove ConveyanceRefNumberPage in 15/13/170" in {
          forAll(nonEmptyString) {
            conveyanceRefNumber =>
              val userAnswers = emptyUserAnswers
                .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)

              val result = userAnswers.setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)

              result.get(ConveyanceReferenceNumberPage(activeIndex)) must not be defined
              result.departureData.Consignment.ActiveBorderTransportMeans
                .flatMap(_.lift(activeIndex.position).flatMap(_.conveyanceReferenceNumber)) must not be defined

          }
        }
      }
    }

  }

}
