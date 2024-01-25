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

import base.TestMessageData.{contactName, representative}
import base.{SpecBase, TestMessageData}
import pages.representative.NamePage

class RepresentativeNameTransformerSpec extends SpecBase {
  val transformer = new RepresentativeNameTransformer()

  "RepresentativeNameTransformer" - {
    "must return updated answers with representative NamePage" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(NamePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(NamePage) mustBe Some(contactName)
      }
    }

    "must not update if representative phone name is None" in {
      val userAnswers =
        emptyUserAnswers.copy(departureData =
          TestMessageData.messageData.copy(
            Representative = Some(representative.copy(ContactPerson = None))
          )
        )

      userAnswers.get(NamePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(NamePage) mustBe None
      }
    }
  }
}
