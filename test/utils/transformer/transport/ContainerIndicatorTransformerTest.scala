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
import generated._
import generators.Generators
import pages.transport.ContainerIndicatorPage

class ContainerIndicatorTransformerTest extends SpecBase with Generators {
  val transformer = new ContainerIndicatorTransformer()

  "ContainerIndicatorPageTransformer" - {
    "must return updated answers with ContainerIndicatorPage" - {
      "when true" in {
        val userAnswers = setContainerIndicatorOnUserAnswersLens.replace(
          Some(Number1)
        )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(ContainerIndicatorPage) mustBe Some(true)
      }

      "when false" in {
        val userAnswers = setContainerIndicatorOnUserAnswersLens.replace(
          Some(Number0)
        )(emptyUserAnswers)

        val result = transformer.transform.apply(userAnswers).futureValue
        result.get(ContainerIndicatorPage) mustBe Some(false)
      }
    }

    "must not update if ContainerIndicatorPage is None" in {
      val userAnswers = setContainerIndicatorOnUserAnswersLens.replace(
        None
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(ContainerIndicatorPage) mustBe None
    }
  }
}
