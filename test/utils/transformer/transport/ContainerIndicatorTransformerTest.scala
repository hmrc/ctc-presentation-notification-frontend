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
import base.TestMessageData.consignment
import pages.transport.ContainerIndicatorPage

class ContainerIndicatorTransformerTest extends SpecBase {
  val transformer = new ContainerIndicatorTransformer()

  "ContainerIndicatorPageTransformer" - {
    "must return updated answers with ContainerIndicatorPage" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(ContainerIndicatorPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ContainerIndicatorPage) mustBe Some(true)
      }
    }

    "must not update if ContainerIndicatorPage is None" in {
      val userAnswers =
        emptyUserAnswers.copy(departureData = emptyUserAnswers.departureData.copy(Consignment = consignment.copy(containerIndicator = None)))
      userAnswers.get(ContainerIndicatorPage) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ContainerIndicatorPage) mustBe None
      }
    }
  }
}
