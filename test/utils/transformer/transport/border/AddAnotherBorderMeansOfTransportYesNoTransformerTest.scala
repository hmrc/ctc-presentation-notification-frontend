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
import generated._
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.border.AddAnotherBorderMeansOfTransportYesNoPage

class AddAnotherBorderMeansOfTransportYesNoTransformerTest extends SpecBase with Generators {
  val transformer = new AddAnotherBorderMeansOfTransportYesNoTransformer()

  "AddAnotherBorderMeansOfTransportYesNoTransformer" - {

    "must skip transforming if there is no border means" in {
      val userAnswers = setBorderMeansAnswersLens.replace(
        Nil
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result mustBe userAnswers
    }

    "must return updated answers with Add Another yes/no answers" - {
      "when there is 1 or more border means, answer should be No (false)" in {
        forAll(arbitrary[ActiveBorderTransportMeansType02]) {
          borderTransportMeans =>
            val userAnswers = setBorderMeansAnswersLens.replace(
              Seq(borderTransportMeans)
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(AddAnotherBorderMeansOfTransportYesNoPage(Index(0))).get mustBe false
        }
      }
    }
  }
}
