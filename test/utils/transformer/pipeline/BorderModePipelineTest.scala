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
import utils.transformer.transport.border._

import scala.concurrent.ExecutionContext.Implicits.global

class BorderModePipelineTest extends SpecBase {

  "BorderModePipeline" - {
    "should call all transformers" in {

      val modeOfTransportAtTheBorderTransformer    = mock[ModeOfTransportAtTheBorderTransformer]
      val addBorderModeOfTransportYesNoTransformer = mock[AddBorderModeOfTransportYesNoTransformer]

      when(modeOfTransportAtTheBorderTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addBorderModeOfTransportYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new BorderModePipeline(
        modeOfTransportAtTheBorderTransformer,
        addBorderModeOfTransportYesNoTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(modeOfTransportAtTheBorderTransformer, times(1)).transform(hc)
      verify(addBorderModeOfTransportYesNoTransformer, times(1)).transform(hc)
    }
  }
}
