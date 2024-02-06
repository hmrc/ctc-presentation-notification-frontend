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

import models.Index
import models.reference.Nationality
import models.reference.TransportMode.BorderMode
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.transport.border.active.{IdentificationNumberPage, NationalityPage}

class BorderModeOfTransportPageSpec extends PageBehaviours {

  "BorderModeOfTransportPage" - {

    beRetrievable[BorderMode](BorderModeOfTransportPage)

    beSettable[BorderMode](BorderModeOfTransportPage)

    beRemovable[BorderMode](BorderModeOfTransportPage)
  }

  "cleanup" - {
    "when code is changed" in {
      forAll(arbitrary[BorderMode]) {
        borderMode =>
          val userAnswers = emptyUserAnswers
            .setValue(AddBorderMeansOfTransportYesNoPage, true)
            .setValue(NationalityPage(Index(0)), Nationality("GB", "United Kingdom"))
            .setValue(IdentificationNumberPage(Index(0)), "12345")

          val result = userAnswers.setValue(BorderModeOfTransportPage, borderMode)

          result.get(AddBorderMeansOfTransportYesNoPage) mustNot be(defined)
          result.get(NationalityPage(Index(0))) mustNot be(defined)
          result.get(IdentificationNumberPage(Index(0))) mustNot be(defined)

      }

    }
  }

}
