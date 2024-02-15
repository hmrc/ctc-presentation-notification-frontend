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
import utils.transformer.departureTransportMeans.{
  TransportMeansIdentificationNumberTransformer,
  TransportMeansIdentificationTransformer,
  TransportMeansNationalityTransformer
}

import scala.concurrent.ExecutionContext.Implicits.global

class DepartureTransportMeansPipelineTest extends SpecBase {

  "DepartureTransportMeansPipeline" - {
    "should call all transformers" in {

      val transportMeansIdentificationTransformer       = mock[TransportMeansIdentificationTransformer]
      val transportMeansIdentificationNumberTransformer = mock[TransportMeansIdentificationNumberTransformer]
      val transportMeansNationalityTransformer          = mock[TransportMeansNationalityTransformer]

      when(transportMeansIdentificationTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(transportMeansIdentificationNumberTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(transportMeansNationalityTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new DepartureTransportMeansPipeline(
        transportMeansIdentificationTransformer,
        transportMeansIdentificationNumberTransformer,
        transportMeansNationalityTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(transportMeansIdentificationTransformer, times(1)).transform(hc)
      verify(transportMeansIdentificationNumberTransformer, times(1)).transform(hc)
      verify(transportMeansNationalityTransformer, times(1)).transform(hc)
    }
  }
}
