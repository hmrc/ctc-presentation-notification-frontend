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
import utils.transformer.transport.placeOfLoading._

import scala.concurrent.ExecutionContext.Implicits.global

class PlaceOfLoadingPipelineTest extends SpecBase {

  "PlaceOfLoadingPipeline" - {
    "should call all transformers" in {

      val unLocodeTransformer                 = mock[UnLocodeTransformer]
      val addUnLocodeYesNoTransformer         = mock[AddUnLocodeYesNoTransformer]
      val addExtraInformationYesNoTransformer = mock[AddExtraInformationYesNoTransformer]
      val locationTransformer                 = mock[LocationTransformer]
      val countryTransformer                  = mock[CountryTransformer]

      when(unLocodeTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addUnLocodeYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(addExtraInformationYesNoTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(locationTransformer.transform(hc)).thenReturn(defaultTransformFunction)
      when(countryTransformer.transform(hc)).thenReturn(defaultTransformFunction)

      val pipeline = new PlaceOfLoadingPipeline(
        unLocodeTransformer,
        addUnLocodeYesNoTransformer,
        addExtraInformationYesNoTransformer,
        locationTransformer,
        countryTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(unLocodeTransformer, times(1)).transform(hc)
      verify(addUnLocodeYesNoTransformer, times(1)).transform(hc)
      verify(addExtraInformationYesNoTransformer, times(1)).transform(hc)
      verify(locationTransformer, times(1)).transform(hc)
      verify(countryTransformer, times(1)).transform(hc)
    }
  }
}
