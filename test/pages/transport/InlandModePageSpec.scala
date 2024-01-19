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

import models.reference.TransportMode.InlandMode
import pages.behaviours.PageBehaviours
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import pages.houseConsignment.index.departureTransportMeans.{CountryPage, IdentificationNumberPage, IdentificationPage}
import pages.sections.houseConsignment.HouseConsignmentListSection

class InlandModePageSpec extends PageBehaviours {

  "InlandModePage" - {

    beRetrievable[InlandMode](InlandModePage)

    beSettable[InlandMode](InlandModePage)

    beRemovable[InlandMode](InlandModePage)

    "cleanup" - {
      "when option 5 selected" - {
        "must remove HouseConsignmentSection in 170" in {
          forAll(arbitraryTransportMeansIdentification.arbitrary, nonEmptyString, arbitraryNationality.arbitrary) {
            (identification, identificationNumber, nationality) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
                .setValue(IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identification)
                .setValue(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identificationNumber)
                .setValue(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), nationality)

              val result = userAnswers.setValue(InlandModePage, InlandMode("5", "test"))

              result.get(HouseConsignmentListSection) must not be defined

          }
        }
      }
    }
  }
}
