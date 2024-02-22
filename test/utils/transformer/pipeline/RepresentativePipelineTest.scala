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

package utils.transformer.pipeline

import base.SpecBase
import org.mockito.Mockito.{times, verify, when}
import utils.transformer.Helper.{defaultTransformFunction, userAnswers}
import utils.transformer.representative._

import scala.concurrent.ExecutionContext.Implicits.global

class RepresentativePipelineTest extends SpecBase {

  "RepresentativePipeline" - {
    "should call all transformers" in {

      val actingAsRepresentativeTransformer               = mock[ActingAsRepresentativeTransformer]
      val representativeEoriTransformer                   = mock[RepresentativeEoriTransformer]
      val addRepresentativeContactDetailsYesNoTransformer = mock[AddRepresentativeContactDetailsYesNoTransformer]
      val representativeNameTransformer                   = mock[RepresentativeNameTransformer]
      val representativePhoneNumberTransformer            = mock[RepresentativePhoneNumberTransformer]

      when(actingAsRepresentativeTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(representativeEoriTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addRepresentativeContactDetailsYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(representativeNameTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(representativePhoneNumberTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new RepresentativePipeline(
        actingAsRepresentativeTransformer,
        representativeEoriTransformer,
        addRepresentativeContactDetailsYesNoTransformer,
        representativeNameTransformer,
        representativePhoneNumberTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(actingAsRepresentativeTransformer, times(1)).transform(hc)
      verify(representativeEoriTransformer, times(1)).transform(hc)
      verify(addRepresentativeContactDetailsYesNoTransformer, times(1)).transform(hc)
      verify(representativeNameTransformer, times(1)).transform(hc)
      verify(representativePhoneNumberTransformer, times(1)).transform(hc)
    }
  }
}
