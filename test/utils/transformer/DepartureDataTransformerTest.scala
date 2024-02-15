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

package utils.transformer

import base.SpecBase
import org.mockito.Mockito.{times, verify, when}
import utils.transformer.Helper.{defaultTransformFunction, userAnswers}
import utils.transformer.pipeline._

import scala.concurrent.ExecutionContext.Implicits.global

class DepartureDataTransformerTest extends SpecBase {

  "DepartureDataTransformer" - {
    "should call all transformers" in {
      val borderMeansPipeline             = mock[BorderMeansPipeline]
      val borderModePipeline              = mock[BorderModePipeline]
      val departureTransportMeansPipeline = mock[DepartureTransportMeansPipeline]
      val locationOfGoodsPipeline         = mock[LocationOfGoodsPipeline]
      val placeOfLoadingPipeline          = mock[PlaceOfLoadingPipeline]
      val representativePipeline          = mock[RepresentativePipeline]
      val transportPipeline               = mock[TransportPipeline]
      val transportEquipmentPipeline      = mock[TransportEquipmentPipeline]

      when(borderMeansPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(borderModePipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(departureTransportMeansPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(locationOfGoodsPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(placeOfLoadingPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(representativePipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(transportPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)
      when(transportEquipmentPipeline.pipeline(hc)).thenReturn(defaultTransformFunction)

      val departureDataTransformer = new DepartureDataTransformer(
        borderMeansPipeline,
        borderModePipeline,
        departureTransportMeansPipeline,
        locationOfGoodsPipeline,
        placeOfLoadingPipeline,
        representativePipeline,
        transportPipeline,
        transportEquipmentPipeline
      )

      whenReady(departureDataTransformer.transform(userAnswers)) {
        _ =>
          verify(borderMeansPipeline, times(1)).pipeline(hc)
          verify(borderModePipeline, times(1)).pipeline(hc)
          verify(departureTransportMeansPipeline, times(1)).pipeline(hc)
          verify(locationOfGoodsPipeline, times(1)).pipeline(hc)
          verify(placeOfLoadingPipeline, times(1)).pipeline(hc)
          verify(representativePipeline, times(1)).pipeline(hc)
          verify(transportPipeline, times(1)).pipeline(hc)
          verify(transportEquipmentPipeline, times(1)).pipeline(hc)
      }
    }
  }
}
