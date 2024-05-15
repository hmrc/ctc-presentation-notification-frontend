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
import generated.ActiveBorderTransportMeansType02
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.border.active.IdentificationNumberPage

class IdentificationNumberTransformerTest extends SpecBase with Generators {
  val transformer = new IdentificationNumberTransformer()

  "IdentificationNumberTransformer" - {

    "must skip transforming if there is no border means" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02]) {
        borderTransportMeans =>
          val userAnswers = setBorderMeansAnswersLens.set(
            Seq(borderTransportMeans.copy(identificationNumber = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(IdentificationNumberPage(Index(0))) mustBe None
      }
    }

    "must return updated answers with IdentificationNumberPage" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02], nonEmptyString) {
        (borderTransportMeans, identificationNumber) =>
          val userAnswers = setBorderMeansAnswersLens.set(
            Seq(borderTransportMeans.copy(identificationNumber = Some(identificationNumber)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(IdentificationNumberPage(Index(0))).value mustBe identificationNumber
      }
    }
  }
}
