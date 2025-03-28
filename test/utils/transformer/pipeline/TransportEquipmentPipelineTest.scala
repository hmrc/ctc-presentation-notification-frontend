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
import models.UserAnswers
import org.mockito.Mockito.{times, verify, when}
import utils.transformer.Helper.{userAnswers, verifyTransformer}
import utils.transformer.transport.equipment._

import scala.concurrent.ExecutionContext.Implicits.global

class TransportEquipmentPipelineTest extends SpecBase {

  "TransportEquipmentPipeline" - {
    "should call all transformers" in {

      val transportEquipmentYesNoTransformer            = mock[TransportEquipmentYesNoTransformer]
      val containerIdentificationNumberTransformer      = mock[ContainerIdentificationNumberTransformer]
      val containerIdentificationNumberYesNoTransformer = mock[ContainerIdentificationNumberYesNoTransformer]
      val sealTransformer                               = mock[SealTransformer]
      val sealYesNoTransformer                          = mock[AddSealYesNoTransformer]
      val itemTransformer                               = mock[ItemTransformer]

      val uaWithEquipment = mock[UserAnswers]

      when(containerIdentificationNumberYesNoTransformer.transform(hc)).thenReturn(verifyTransformer(expect = userAnswers, `return` = userAnswers))
      when(transportEquipmentYesNoTransformer.transform(hc)).thenReturn(verifyTransformer(expect = userAnswers, `return` = userAnswers))
      when(containerIdentificationNumberTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithEquipment, `return` = uaWithEquipment))
      when(sealTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithEquipment, `return` = uaWithEquipment))
      when(sealYesNoTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithEquipment, `return` = uaWithEquipment))
      when(itemTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithEquipment, `return` = uaWithEquipment))

      val pipeline = new TransportEquipmentPipeline(
        transportEquipmentYesNoTransformer,
        containerIdentificationNumberTransformer,
        containerIdentificationNumberYesNoTransformer,
        sealTransformer,
        sealYesNoTransformer,
        itemTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(transportEquipmentYesNoTransformer, times(1)).transform(hc)
      verify(containerIdentificationNumberTransformer, times(1)).transform(hc)
      verify(containerIdentificationNumberYesNoTransformer, times(1)).transform(hc)
      verify(sealTransformer, times(1)).transform(hc)
      verify(sealYesNoTransformer, times(1)).transform(hc)
    }
  }
}
