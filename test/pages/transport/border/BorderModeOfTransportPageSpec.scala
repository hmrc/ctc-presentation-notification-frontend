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

import models.reference.TransportMode.BorderMode
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.transport.border.BorderActiveListSection
import play.api.libs.json.{JsArray, Json}

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
            .setValue(BorderActiveListSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

          val result = userAnswers.setValue(BorderModeOfTransportPage, borderMode)

          result.get(AddBorderModeOfTransportYesNoPage).value mustBe true
          result.get(AddBorderMeansOfTransportYesNoPage) mustNot be(defined)
          result.get(BorderActiveListSection) mustNot be(defined)
      }
    }
  }
}
