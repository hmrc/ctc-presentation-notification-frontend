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
import utils.transformer.transport.{AddInlandModeYesNoTransformer, ContainerIndicatorTransformer, InlandModeTransformer, LimitDateTransformer}

import scala.concurrent.ExecutionContext.Implicits.global

class TransportPipelineTest extends SpecBase {

  "TransportPipeline" - {
    "should call all transformers" in {

      val inlandModeTransformer         = mock[InlandModeTransformer]
      val addInlandModeYesNoTransformer = mock[AddInlandModeYesNoTransformer]
      val containerIndicatorTransformer = mock[ContainerIndicatorTransformer]
      val limitDateTransformer          = mock[LimitDateTransformer]

      when(inlandModeTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addInlandModeYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(containerIndicatorTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(limitDateTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new TransportPipeline(
        inlandModeTransformer,
        addInlandModeYesNoTransformer,
        containerIndicatorTransformer,
        limitDateTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(inlandModeTransformer, times(1)).transform(hc)
      verify(addInlandModeYesNoTransformer, times(1)).transform(hc)
      verify(containerIndicatorTransformer, times(1)).transform(hc)
      verify(limitDateTransformer, times(1)).transform(hc)
    }
  }
}
