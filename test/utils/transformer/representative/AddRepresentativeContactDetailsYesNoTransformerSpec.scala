/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformer.representative

import base.SpecBase
import base.TestMessageData.representativeEori
import models.messages.Representative
import pages.representative.AddRepresentativeContactDetailsYesNoPage

class AddRepresentativeContactDetailsYesNoTransformerSpec extends SpecBase {

  val transformer = new AddRepresentativeContactDetailsYesNoTransformer()

  "AddRepresentativeContactDetailsYesNoTransformer" - {
    "when representative contact details is present must return updated answers with AddRepresentativeContactDetailsYesNoPage as true" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(AddRepresentativeContactDetailsYesNoPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddRepresentativeContactDetailsYesNoPage) mustBe Some(true)
      }
    }

    "when representative contact details not present must return updated answers with AddRepresentativeContactDetailsYesNoPage as false" in {
      val userAnswers = setRepresentativeOnUserAnswersLens.set(Option(Representative(representativeEori, "2", None)))(emptyUserAnswers)

      userAnswers.get(AddRepresentativeContactDetailsYesNoPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddRepresentativeContactDetailsYesNoPage) mustBe Some(false)
      }
    }
  }
}
