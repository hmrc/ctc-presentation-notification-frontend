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

package utils.transformer.transport.placeOfLoading

import base.SpecBase
import pages.loading.UnLocodePage

class UnLocodeTransformerTest extends SpecBase {

  val transformer = new UnLocodeTransformer()

  "UnLocodeTransformer" - {
    "must return updated answers with UnLocodePage" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(UnLocodePage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(UnLocodePage) mustBe Some("UNCODEX")
      }

    }
  }
}
