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
import utils.transformer.transport._
import utils.transformer.transport.border._
import utils.transformer.transport.equipment._
import utils.transformer.transport.LimitDateTransformer
import utils.transformer.transport.{AddInlandModeYesNoTransformer, InlandModeTransformer}
import utils.transformer.transport.border.{IdentificationNumberTransformer, IdentificationTransformer}
import utils.transformer.transport.equipment.{ContainerIdentificationNumberTransformer, SealTransformer, TransportEquipmentTransformer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class DepartureDataTransformerTest extends SpecBase {

  "DepartureDataTransformer" - {
    "should call all transformers" in {
      val identificationTransformer                     = mock[IdentificationTransformer]
      val identificationNumberTransformer               = mock[IdentificationNumberTransformer]
      val transportEquipmentTransformer                 = mock[TransportEquipmentTransformer]
      val containerIdentificationNumberTransformer      = mock[ContainerIdentificationNumberTransformer]
      val sealTransformer                               = mock[SealTransformer]
      val limitDateTransformer                          = mock[LimitDateTransformer]
      val transportMeansIdentificationNumberTransformer = mock[TransportMeansIdentificationNumberTransformer]
      val transportMeansIdentificationTransformer       = mock[TransportMeansIdentificationTransformer]
      val transportMeansNationalityTransformer          = mock[TransportMeansNationalityTransformer]
      val userAnswers                                   = mock[UserAnswers]
      val userAnswersWithEquipment                      = mock[UserAnswers]
      val identificationTransformer                = mock[IdentificationTransformer]
      val identificationNumberTransformer          = mock[IdentificationNumberTransformer]
      val inlandModeTransformer                    = mock[InlandModeTransformer]
      val addInlandModeYesNoTransformer            = mock[AddInlandModeYesNoTransformer]
      val transportEquipmentTransformer            = mock[TransportEquipmentTransformer]
      val containerIdentificationNumberTransformer = mock[ContainerIdentificationNumberTransformer]
      val sealTransformer                          = mock[SealTransformer]
      val limitDateTransformer                     = mock[LimitDateTransformer]
      val userAnswers                              = mock[UserAnswers]
      val userAnswersWithEquipment                 = mock[UserAnswers]

      val verifyTransportEquipmentTransformersOrder: UserAnswers => Future[UserAnswers] = {
        input =>
          if (input != userAnswersWithEquipment) fail("This transformer must be called after transportEquipmentTransformer")
          else successful(input)
      }

      when(identificationTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )
      when(identificationNumberTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(inlandModeTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(addInlandModeYesNoTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportEquipmentTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswersWithEquipment)
      )

      // this should be called after transportEquipmentTransformer because of parent-child relation
      when(containerIdentificationNumberTransformer.transform(hc)).thenReturn(verifyTransportEquipmentTransformersOrder)
      when(sealTransformer.transform(hc)).thenReturn(verifyTransportEquipmentTransformersOrder)

      when(limitDateTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportMeansIdentificationNumberTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportMeansIdentificationTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportMeansNationalityTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      val departureDataTransformer = new DepartureDataTransformer(
        identificationTransformer,
        identificationNumberTransformer,
        inlandModeTransformer,
        addInlandModeYesNoTransformer,
        transportEquipmentTransformer,
        containerIdentificationNumberTransformer,
        sealTransformer,
        limitDateTransformer,
        transportMeansIdentificationNumberTransformer,
        transportMeansIdentificationTransformer,
        transportMeansNationalityTransformer
      )

      whenReady(departureDataTransformer.transform(userAnswers)) {
        _ =>
          verify(identificationTransformer, times(1)).transform(hc)
          verify(identificationNumberTransformer, times(1)).transform(hc)
          verify(inlandModeTransformer, times(1)).transform(hc)
          verify(addInlandModeYesNoTransformer, times(1)).transform(hc)
          verify(transportEquipmentTransformer, times(1)).transform(hc)
          verify(containerIdentificationNumberTransformer, times(1)).transform(hc)
          verify(sealTransformer, times(1)).transform(hc)
          verify(limitDateTransformer, times(1)).transform(hc)
          verify(transportMeansIdentificationNumberTransformer, times(1)).transform(hc)
          verify(transportMeansIdentificationTransformer, times(1)).transform(hc)
          verify(transportMeansNationalityTransformer, times(1)).transform(hc)
      }
    }
  }
}
