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
import base.TestMessageData.transportMeansIdentificationNumber
import models.Index
import pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage
import utils.transformer.departureTransportMeans.TransportMeansIdentificationNumberTransformer

class TransportMeansIdentificationNumberTransformerTest extends SpecBase {
  val identificationNumber = transportMeansIdentificationNumber
  val transformer          = new TransportMeansIdentificationNumberTransformer()

  "TransportMeansIdentificationNumberTransformer" - {
    "must return updated answers with TransportMeansIdentificationNumberPage" in {
      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(TransportMeansIdentificationNumberPage(index)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(TransportMeansIdentificationNumberPage(index)) mustBe Some(identificationNumber)
      }
    }
  }
}
