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
import base.TestMessageData.borderTransportMeans
import models.Index
import org.scalacheck.Gen
import pages.transport.border.AddAnotherBorderMeansOfTransportYesNoPage

class AddAnotherBorderMeansOfTransportYesNoTransformerTest extends SpecBase {
  val transformer = new AddAnotherBorderMeansOfTransportYesNoTransformer()

  "AddAnotherBorderMeansOfTransportYesNoTransformer" - {

    "must skip transforming if there is no border means" in {
      forAll(Gen.oneOf(Option(List()), None)) {
        borderMeans =>
          val userAnswers = setBorderMeansAnswersLens.set(borderMeans)(emptyUserAnswers)
          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers mustBe userAnswers
          }
      }
    }

    "must return updated answers with Add Another yes/no answers" - {
      "when there is 1 border means, answer should be No (false)" in {
        val listWith1Item = List(borderTransportMeans)
        val userAnswers   = setBorderMeansAnswersLens.set(Option(listWith1Item))(emptyUserAnswers)
        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(AddAnotherBorderMeansOfTransportYesNoPage(Index(0))).get mustBe false
        }
      }

      "when there are more than 1 border means, only the last answer should be No (false)" in {
        val listWith3Item = List(borderTransportMeans, borderTransportMeans, borderTransportMeans)
        val userAnswers   = setBorderMeansAnswersLens.set(Option(listWith3Item))(emptyUserAnswers)
        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(AddAnotherBorderMeansOfTransportYesNoPage(Index(0))).get mustBe true
            updatedUserAnswers.get(AddAnotherBorderMeansOfTransportYesNoPage(Index(1))).get mustBe true
            updatedUserAnswers.get(AddAnotherBorderMeansOfTransportYesNoPage(Index(2))).get mustBe false
        }
      }
    }
  }
}
