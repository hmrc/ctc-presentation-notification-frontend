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

package utils.transformer.transport.border

import base.SpecBase
import base.TestMessageData.activeBorderTransportMeans
import models.UserAnswers
import pages.transport.border.AddBorderMeansOfTransportYesNoPage

class AddBorderMeansOfTransportYesNoTransformerTest extends SpecBase {
  val transformer = new AddBorderMeansOfTransportYesNoTransformer()

  "AddBorderMeansOfTransportYesNoTransformer" - {
    "must return AddBorderMeansOfTransportYesNoPage Yes (true) when there is at least 1 border means" in {
      val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(Option(activeBorderTransportMeans))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddBorderMeansOfTransportYesNoPage).get mustBe true
      }
    }

    "must return AddBorderMeansOfTransportYesNoPage No (false) when there is no border means" in {
      val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(Option(Seq()))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddBorderMeansOfTransportYesNoPage).get mustBe false
      }
    }
  }
}
