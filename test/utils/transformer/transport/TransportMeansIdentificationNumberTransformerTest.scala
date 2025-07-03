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
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage
import utils.transformer.departureTransportMeans.TransportMeansIdentificationNumberTransformer

class TransportMeansIdentificationNumberTransformerTest extends SpecBase with Generators {

  val transformer = new TransportMeansIdentificationNumberTransformer()

  "TransportMeansIdentificationNumberTransformer" - {
    "must return updated answers with TransportMeansIdentificationNumberPage" in {
      forAll(arbitrary[DepartureTransportMeansType01], nonEmptyString) {
        (departureTransportMeans, identificationNumber) =>
          val userAnswers = setDepartureTransportMeansAnswersLens.replace(
            Seq(departureTransportMeans.copy(identificationNumber = identificationNumber))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(TransportMeansIdentificationNumberPage(Index(0))).value mustBe identificationNumber
      }
    }
  }
}
