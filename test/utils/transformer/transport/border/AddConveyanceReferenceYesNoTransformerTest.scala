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
import pages.transport.border.active.AddConveyanceReferenceYesNoPage

class AddConveyanceReferenceYesNoTransformerTest extends SpecBase with Generators {
  val transformer = new AddConveyanceReferenceYesNoTransformer()

  "AddConveyanceReferenceYesNoTransformer" - {

    "must skip transforming if there is no conveyance reference number" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02]) {
        borderTransportMeans =>
          val userAnswers = setBorderMeansAnswersLens.replace(
            Seq(borderTransportMeans.copy(conveyanceReferenceNumber = None))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddConveyanceReferenceYesNoPage(Index(0))).value mustBe false
      }
    }

    "must return AddConveyanceReferenceYesNoPage Yes (true) when there is conveyance reference" in {
      forAll(arbitrary[ActiveBorderTransportMeansType02], nonEmptyString) {
        (borderTransportMeans, conveyanceReferenceNumber) =>
          val userAnswers = setBorderMeansAnswersLens.replace(
            Seq(borderTransportMeans.copy(conveyanceReferenceNumber = Some(conveyanceReferenceNumber)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddConveyanceReferenceYesNoPage(Index(0))).value mustBe true
      }
    }
  }
}
