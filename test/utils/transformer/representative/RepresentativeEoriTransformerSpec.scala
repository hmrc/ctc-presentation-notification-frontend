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

import base.TestMessageData.representativeEori
import base.{SpecBase, TestMessageData}
import pages.representative.EoriPage

class RepresentativeEoriTransformerSpec extends SpecBase {
  val transformer = new RepresentativeEoriTransformer()

  "RepresentativeEoriTransformer" - {
    "must return updated answers with representative EoriPage" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(EoriPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(EoriPage) mustBe Some(representativeEori)
      }
    }

    "must not update if representative eori is None" in {
      val userAnswers =
        emptyUserAnswers.copy(departureData =
          TestMessageData.messageData.copy(
            Representative = None
          )
        )

      userAnswers.get(EoriPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(EoriPage) mustBe None
      }
    }
  }
}
