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
import utils.transformer.transport.border.*

import scala.concurrent.ExecutionContext.Implicits.global

class BorderMeansPipelineTest extends SpecBase {

  "BorderMeansPipeline" - {
    "should call all transformers" in {

      val addBorderMeansOfTransportYesNoTransformer = mock[AddBorderMeansOfTransportYesNoTransformer]
      val addConveyanceReferenceYesNoTransformer    = mock[AddConveyanceReferenceYesNoTransformer]
      val conveyanceReferenceTransformer            = mock[ConveyanceReferenceTransformer]
      val customsOfficeTransformer                  = mock[CustomsOfficeTransformer]
      val identificationTransformer                 = mock[IdentificationTransformer]
      val identificationNumberTransformer           = mock[IdentificationNumberTransformer]
      val nationalityTransformer                    = mock[NationalityTransformer]

      when(addBorderMeansOfTransportYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addConveyanceReferenceYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(conveyanceReferenceTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(customsOfficeTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(identificationTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(identificationNumberTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(nationalityTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new BorderMeansPipeline(
        addBorderMeansOfTransportYesNoTransformer,
        addConveyanceReferenceYesNoTransformer,
        conveyanceReferenceTransformer,
        customsOfficeTransformer,
        identificationTransformer,
        identificationNumberTransformer,
        nationalityTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(addBorderMeansOfTransportYesNoTransformer, times(1)).transform(hc)
      verify(addConveyanceReferenceYesNoTransformer, times(1)).transform(hc)
      verify(conveyanceReferenceTransformer, times(1)).transform(hc)
      verify(customsOfficeTransformer, times(1)).transform(hc)
      verify(identificationTransformer, times(1)).transform(hc)
      verify(identificationNumberTransformer, times(1)).transform(hc)
      verify(nationalityTransformer, times(1)).transform(hc)
    }
  }
}
