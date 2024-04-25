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
import models.{Index, UserAnswers}
import org.scalacheck.Gen
import pages.transport.border.active.AddConveyanceReferenceYesNoPage

class AddConveyanceReferenceYesNoTransformerTest extends SpecBase {
  val transformer = new AddConveyanceReferenceYesNoTransformer()

  "AddConveyanceReferenceYesNoTransformer" - {

    "must skip transforming if there is no border means" in {
      forAll(Gen.oneOf(Option(List()), None)) {
        borderMeans =>
          val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(borderMeans)(emptyUserAnswers)
          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers mustBe userAnswers
          }
      }
    }

    "must return AddConveyanceReferenceYesNoPage Yes (true) when there is conveyance reference" in {
      val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(Option(List(borderTransportMeans, borderTransportMeans)))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddConveyanceReferenceYesNoPage(Index(0))).get mustBe true
          updatedUserAnswers.get(AddConveyanceReferenceYesNoPage(Index(1))).get mustBe true
      }
    }
  }
}
