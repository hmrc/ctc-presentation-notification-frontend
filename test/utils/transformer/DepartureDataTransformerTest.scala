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
import models.UserAnswers
import org.mockito.Mockito.{times, verify, when}
import utils.transformer.transport.border.{IdentificationNumberTransformer, IdentificationTransformer}
import utils.transformer.transport.eqipment.{ContainerIdentificationNumberTransformer, SealTransformer, TransportEquipmentTransformer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class DepartureDataTransformerTest extends SpecBase {

  "DepartureDataTransformer" - {
    "should call all transformers" in {
      val identificationTransformer                = mock[IdentificationTransformer]
      val identificationNumberTransformer          = mock[IdentificationNumberTransformer]
      val transportEquipmentTransformer            = mock[TransportEquipmentTransformer]
      val containerIdentificationNumberTransformer = mock[ContainerIdentificationNumberTransformer]
      val sealTransformer                          = mock[SealTransformer]
      val userAnswers                              = mock[UserAnswers]
      val userAnswersWithEquipment                 = mock[UserAnswers]

      when(identificationTransformer.transform(userAnswers)).thenReturn(successful(userAnswers))
      when(identificationNumberTransformer.transform(userAnswers)).thenReturn(successful(userAnswers))
      when(transportEquipmentTransformer.transform(userAnswers)).thenReturn(successful(userAnswersWithEquipment))

      // this should be called after transportEquipmentTransformer because of parent-child relation
      when(containerIdentificationNumberTransformer.transform(userAnswersWithEquipment)).thenReturn(successful(userAnswersWithEquipment))
      when(sealTransformer.transform(userAnswersWithEquipment)).thenReturn(successful(userAnswersWithEquipment))

      val departureDataTransformer = new DepartureDataTransformer(
        identificationTransformer,
        identificationNumberTransformer,
        transportEquipmentTransformer,
        containerIdentificationNumberTransformer,
        sealTransformer
      )

      whenReady(departureDataTransformer.transform(userAnswers)) {
        _ =>
          verify(identificationTransformer, times(1)).transform(userAnswers)
          verify(identificationNumberTransformer, times(1)).transform(userAnswers)
          verify(transportEquipmentTransformer, times(1)).transform(userAnswers)
          verify(containerIdentificationNumberTransformer, times(1)).transform(userAnswersWithEquipment)
          verify(sealTransformer, times(1)).transform(userAnswersWithEquipment)
      }
    }
  }
}
