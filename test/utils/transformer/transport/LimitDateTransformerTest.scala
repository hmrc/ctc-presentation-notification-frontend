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

package utils.transformer.transport

import base.SpecBase
import base.TestMessageData.{limitDate, messageData}
import pages.transport.LimitDatePage

import java.time.LocalDate

class LimitDateTransformerTest extends SpecBase {
  val transformer = new LimitDateTransformer()

  "LimitDateTransformer" - {
    "must return updated answers with LimitDatePage" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(LimitDatePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(LimitDatePage) mustBe Some(LocalDate.parse(limitDate))
      }
    }

    "must not update if limit date is None" in {
      val userAnswers =
        emptyUserAnswers.copy(departureData = emptyUserAnswers.departureData.copy(TransitOperation = messageData.TransitOperation.copy(limitDate = None)))
      userAnswers.get(LimitDatePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(LimitDatePage) mustBe None
      }
    }
  }
}
