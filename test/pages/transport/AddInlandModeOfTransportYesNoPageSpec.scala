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

import models.reference.Nationality
import models.reference.TransportMode.InlandMode
import pages.behaviours.PageBehaviours
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage, TransportMeansNationalityPage}

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

        "must remove departure means of transport section in 15/13/170" in {
          forAll(arbitraryInlandModeOfTransport.arbitrary.suchThat(_.code != "5"), arbitraryTransportMeansIdentification.arbitrary) {
            (inlandMode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddInlandModeOfTransportYesNoPage, true)
                .setValue(InlandModePage, inlandMode)
                .setValue(TransportMeansIdentificationPage, identification)
                .setValue(TransportMeansIdentificationNumberPage, "1234")
                .setValue(TransportMeansNationalityPage, Nationality("FR", "France"))

              val result = userAnswers.setValue(InlandModePage, InlandMode("5", "test"))

              result.departureData.Consignment.DepartureTransportMeans must not be defined
          }
        }
      }
    }

  }
}
