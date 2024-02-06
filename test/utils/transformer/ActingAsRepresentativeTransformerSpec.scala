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

package utils.transformer

import base.SpecBase
import models.UserAnswers
import pages.ActingAsRepresentativePage

class ActingAsRepresentativeTransformerSpec extends SpecBase {

  val transformer = new ActingAsRepresentativeTransformer()

  "ActingAsRepresentativeTransformer" - {
    "when representative details is present must return updated answers with ActingAsRepresentative page as true" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(ActingAsRepresentativePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ActingAsRepresentativePage) mustBe Some(true)
      }
    }

    "when representative details not present must return updated answers with ActingAsRepresentative page as false" in {
      val userAnswers = UserAnswers.setRepresentativeOnUserAnswersLens.set(None)(emptyUserAnswers)

      userAnswers.get(ActingAsRepresentativePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ActingAsRepresentativePage) mustBe Some(false)
      }
    }
  }
}
