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
import utils.transformer.representative._
import utils.transformer.transport.LimitDateTransformer
import utils.transformer.transport.border._
import utils.transformer.transport._
import utils.transformer.transport.border.{
  AddBorderModeOfTransportYesNoTransformer,
  IdentificationNumberTransformer,
  IdentificationTransformer,
  ModeOfTransportAtTheBorderTransformer
}
import utils.transformer.transport.equipment.{
  ContainerIdentificationNumberTransformer,
  ContainerIndicatorTransformer,
  SealTransformer,
  TransportEquipmentTransformer
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class DepartureDataTransformerTest extends SpecBase {

  "DepartureDataTransformer" - {
    "should call all transformers" in {
      val addAnotherBorderMeansOfTransportYesNoTransformer = mock[AddAnotherBorderMeansOfTransportYesNoTransformer]
      val addBorderMeansOfTransportYesNoTransformer        = mock[AddBorderMeansOfTransportYesNoTransformer]
      val addConveyanceReferenceYesNoTransformer           = mock[AddConveyanceReferenceYesNoTransformer]
      val conveyanceReferenceTransformer                   = mock[ConveyanceReferenceTransformer]
      val customsOfficeTransformer                         = mock[CustomsOfficeTransformer]
      val addBorderModeOfTransportYesNoTransformer         = mock[AddBorderModeOfTransportYesNoTransformer]
      val identificationTransformer                        = mock[IdentificationTransformer]
      val nationalityTransformer                           = mock[NationalityTransformer]
      val identificationNumberTransformer                  = mock[IdentificationNumberTransformer]
      val transportEquipmentTransformer                    = mock[TransportEquipmentTransformer]
      val containerIdentificationNumberTransformer         = mock[ContainerIdentificationNumberTransformer]
      val sealTransformer                                  = mock[SealTransformer]
      val limitDateTransformer                             = mock[LimitDateTransformer]
      val actingAsRepresentativeTransformer                = mock[ActingAsRepresentativeTransformer]
      val representativeEoriTransformer                    = mock[RepresentativeEoriTransformer]
      val addRepresentativeContactDetailsYesNoTransformer  = mock[AddRepresentativeContactDetailsYesNoTransformer]
      val representativeNameTransformer                    = mock[RepresentativeNameTransformer]
      val representativePhoneNumberTransformer             = mock[RepresentativePhoneNumberTransformer]
      val containerIndicatorTransformer                    = mock[ContainerIndicatorTransformer]
      val modeOfTransportAtTheBorderTransformer            = mock[ModeOfTransportAtTheBorderTransformer]
      val userAnswers                                      = mock[UserAnswers]
      val userAnswersWithEquipment                         = mock[UserAnswers]
      val transportMeansIdentificationNumberTransformer    = mock[TransportMeansIdentificationNumberTransformer]
      val transportMeansIdentificationTransformer          = mock[TransportMeansIdentificationTransformer]
      val transportMeansNationalityTransformer             = mock[TransportMeansNationalityTransformer]
      val inlandModeTransformer                            = mock[InlandModeTransformer]
      val addInlandModeYesNoTransformer                    = mock[AddInlandModeYesNoTransformer]

      val updateAnswersFn: UserAnswers => Future[UserAnswers] = _ => successful(userAnswers)
      val verifyTransportEquipmentTransformersOrder: UserAnswers => Future[UserAnswers] = {
        input =>
          if (input != userAnswersWithEquipment) fail("This transformer must be called after transportEquipmentTransformer")
          else successful(input)
      }

      when(addAnotherBorderMeansOfTransportYesNoTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(addBorderMeansOfTransportYesNoTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(addConveyanceReferenceYesNoTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(conveyanceReferenceTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(customsOfficeTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(identificationTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(identificationNumberTransformer.transform(hc)).thenReturn(updateAnswersFn)
      when(nationalityTransformer.transform(hc)).thenReturn(updateAnswersFn)

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

      when(limitDateTransformer.transform(hc)).thenReturn(updateAnswersFn)

      when(transportMeansIdentificationNumberTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportMeansIdentificationTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(transportMeansNationalityTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(containerIndicatorTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(modeOfTransportAtTheBorderTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(addBorderModeOfTransportYesNoTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(actingAsRepresentativeTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(representativeEoriTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(addRepresentativeContactDetailsYesNoTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(representativeNameTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      when(representativePhoneNumberTransformer.transform(hc)).thenReturn(
        _ => successful(userAnswers)
      )

      val departureDataTransformer = new DepartureDataTransformer(
        addAnotherBorderMeansOfTransportYesNoTransformer,
        addBorderMeansOfTransportYesNoTransformer,
        addConveyanceReferenceYesNoTransformer,
        conveyanceReferenceTransformer,
        customsOfficeTransformer,
        identificationTransformer,
        identificationNumberTransformer,
        inlandModeTransformer,
        addInlandModeYesNoTransformer,
        nationalityTransformer,
        transportEquipmentTransformer,
        containerIdentificationNumberTransformer,
        sealTransformer,
        limitDateTransformer,
        actingAsRepresentativeTransformer,
        representativeEoriTransformer,
        addRepresentativeContactDetailsYesNoTransformer,
        representativeNameTransformer,
        representativePhoneNumberTransformer,
        containerIndicatorTransformer,
        modeOfTransportAtTheBorderTransformer,
        addBorderModeOfTransportYesNoTransformer,
        transportMeansIdentificationTransformer,
        transportMeansIdentificationNumberTransformer,
        transportMeansNationalityTransformer
      )

      whenReady(departureDataTransformer.transform(userAnswers)) {
        _ =>
          verify(addAnotherBorderMeansOfTransportYesNoTransformer, times(1)).transform(hc)
          verify(addBorderMeansOfTransportYesNoTransformer, times(1)).transform(hc)
          verify(addConveyanceReferenceYesNoTransformer, times(1)).transform(hc)
          verify(conveyanceReferenceTransformer, times(1)).transform(hc)
          verify(customsOfficeTransformer, times(1)).transform(hc)
          verify(identificationTransformer, times(1)).transform(hc)
          verify(identificationNumberTransformer, times(1)).transform(hc)
          verify(nationalityTransformer, times(1)).transform(hc)
          verify(inlandModeTransformer, times(1)).transform(hc)
          verify(addInlandModeYesNoTransformer, times(1)).transform(hc)
          verify(transportEquipmentTransformer, times(1)).transform(hc)
          verify(containerIdentificationNumberTransformer, times(1)).transform(hc)
          verify(sealTransformer, times(1)).transform(hc)
          verify(limitDateTransformer, times(1)).transform(hc)
          verify(transportMeansIdentificationTransformer, times(1)).transform(hc)
          verify(transportMeansIdentificationNumberTransformer, times(1)).transform(hc)
          verify(transportMeansNationalityTransformer, times(1)).transform(hc)
          verify(actingAsRepresentativeTransformer, times(1)).transform(hc)
          verify(representativeEoriTransformer, times(1)).transform(hc)
          verify(addRepresentativeContactDetailsYesNoTransformer, times(1)).transform(hc)
          verify(representativeNameTransformer, times(1)).transform(hc)
          verify(representativePhoneNumberTransformer, times(1)).transform(hc)
          verify(containerIndicatorTransformer, times(1)).transform(hc)
          verify(modeOfTransportAtTheBorderTransformer, times(1)).transform(hc)
          verify(addBorderModeOfTransportYesNoTransformer, times(1)).transform(hc)
      }
    }
  }
}
