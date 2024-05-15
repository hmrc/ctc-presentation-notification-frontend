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

package base

import generated._
import models.UserAnswers
import monocle.macros.GenLens
import monocle.std.option._
import monocle.{Lens, Optional}

import javax.xml.datatype.XMLGregorianCalendar

trait Lenses {

  private val departureDataLens: Lens[UserAnswers, CC015CType] =
    GenLens[UserAnswers](_.departureData)

  private val consignmentLens: Lens[CC015CType, ConsignmentType20] =
    GenLens[CC015CType](_.Consignment)

  private val representativeLens: Lens[CC015CType, Option[RepresentativeType05]] =
    GenLens[CC015CType](_.Representative)

  private val modeOfTransportAtTheBorderConsignmentLens: Lens[ConsignmentType20, Option[String]] =
    GenLens[ConsignmentType20](_.modeOfTransportAtTheBorder)

  private val inlandModeOfTransportConsignmentLens: Lens[ConsignmentType20, Option[String]] =
    GenLens[ConsignmentType20](_.inlandModeOfTransport)

  private val containerIndicatorLens: Lens[ConsignmentType20, Option[Flag]] =
    GenLens[ConsignmentType20](_.containerIndicator)

  private val locationOfGoodsConsignmentLens: Lens[ConsignmentType20, Option[LocationOfGoodsType05]] =
    GenLens[ConsignmentType20](_.LocationOfGoods)

  private val placeOfLoadingConsignmentLens: Lens[ConsignmentType20, Option[PlaceOfLoadingType03]] =
    GenLens[ConsignmentType20](_.PlaceOfLoading)

  private val departureTransportMeansConsignmentLens: Lens[ConsignmentType20, Seq[DepartureTransportMeansType03]] =
    GenLens[ConsignmentType20](_.DepartureTransportMeans)

  private val borderMeansConsignmentLens: Lens[ConsignmentType20, Seq[ActiveBorderTransportMeansType02]] =
    GenLens[ConsignmentType20](_.ActiveBorderTransportMeans)

  private val transportEquipmentConsignmentLens: Lens[ConsignmentType20, Seq[TransportEquipmentType06]] =
    GenLens[ConsignmentType20](_.TransportEquipment)

  private val addressLocationLens: Lens[LocationOfGoodsType05, Option[AddressType14]] =
    GenLens[LocationOfGoodsType05](_.Address)

  private val postAddressLocationLens: Lens[LocationOfGoodsType05, Option[PostcodeAddressType02]] =
    GenLens[LocationOfGoodsType05](_.PostcodeAddress)

  private val lensContactPerson: Lens[LocationOfGoodsType05, Option[ContactPersonType06]] =
    GenLens[LocationOfGoodsType05](_.ContactPerson)

  private val lensRepresentativeContactPerson: Lens[RepresentativeType05, Option[ContactPersonType05]] =
    GenLens[RepresentativeType05](_.ContactPerson)

  private val lensAdditionalIdentifier: Lens[LocationOfGoodsType05, Option[String]] =
    GenLens[LocationOfGoodsType05](_.additionalIdentifier)

  val setModeOfTransportAtTheBorderOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.composeLens(consignmentLens).composeLens(modeOfTransportAtTheBorderConsignmentLens)

  val setInlandModeOfTransportOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.composeLens(consignmentLens).composeLens(inlandModeOfTransportConsignmentLens)

  val setContainerIndicatorOnUserAnswersLens: Lens[UserAnswers, Option[Flag]] =
    departureDataLens.composeLens(consignmentLens).composeLens(containerIndicatorLens)

  val setLocationOfGoodsOnUserAnswersLens: Lens[UserAnswers, Option[LocationOfGoodsType05]] =
    departureDataLens.composeLens(consignmentLens).composeLens(locationOfGoodsConsignmentLens)

  val setPlaceOfLoadingOnUserAnswersLens: Lens[UserAnswers, Option[PlaceOfLoadingType03]] =
    departureDataLens.composeLens(consignmentLens).composeLens(placeOfLoadingConsignmentLens)

  val setRepresentativeOnUserAnswersLens: Lens[UserAnswers, Option[RepresentativeType05]] =
    departureDataLens composeLens representativeLens

  val setAddressOnUserAnswersLens: Optional[UserAnswers, AddressType14] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens addressLocationLens composePrism some

  val setPostAddressOnUserAnswersLens: Optional[UserAnswers, PostcodeAddressType02] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens postAddressLocationLens composePrism some

  val setContactPersonOnUserAnswersLens: Optional[UserAnswers, ContactPersonType06] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens lensContactPerson composePrism some

  val setRepresentativeContactPersonDetailsOnUserAnswersLens: Optional[UserAnswers, ContactPersonType05] =
    departureDataLens composeLens representativeLens composePrism some composeLens lensRepresentativeContactPerson composePrism some

  val setAdditionalIdentifierOnUserAnswersLens: Optional[UserAnswers, String] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens lensAdditionalIdentifier composePrism some

  private val customsOfficeReferenceLens: Lens[CC015CType, String] =
    GenLens[CC015CType](_.CustomsOfficeOfDeparture.referenceNumber)

  val setCustomsOfficeDepartureReferenceLens: Lens[UserAnswers, String] =
    departureDataLens composeLens customsOfficeReferenceLens

  private val transitHolderLens: Lens[CC015CType, HolderOfTheTransitProcedureType14] =
    GenLens[CC015CType](_.HolderOfTheTransitProcedure)

  private val transitOperationLens: Lens[CC015CType, TransitOperationType06] =
    GenLens[CC015CType](_.TransitOperation)

  private val limitDateLens: Lens[TransitOperationType06, Option[XMLGregorianCalendar]] =
    GenLens[TransitOperationType06](_.limitDate)

  private val transitHolderEoriLens: Lens[HolderOfTheTransitProcedureType14, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType14](_.identificationNumber)

  private val transitHolderNameLens: Lens[HolderOfTheTransitProcedureType14, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType14](_.name)

  private val transitHolderTirIdentificationLens: Lens[HolderOfTheTransitProcedureType14, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType14](_.TIRHolderIdentificationNumber)

  private val transitHolderContactPersonLens: Lens[HolderOfTheTransitProcedureType14, Option[ContactPersonType05]] =
    GenLens[HolderOfTheTransitProcedureType14](_.ContactPerson)

  private val transitHolderAddressLens: Lens[HolderOfTheTransitProcedureType14, Option[AddressType17]] =
    GenLens[HolderOfTheTransitProcedureType14](_.Address)

  val setTransitHolderEoriLens: Lens[UserAnswers, Option[String]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderEoriLens

  val setLimitDateLens: Lens[UserAnswers, Option[XMLGregorianCalendar]] =
    departureDataLens composeLens transitOperationLens composeLens limitDateLens

  val setTransitHolderNameLens: Lens[UserAnswers, Option[String]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderNameLens

  val setTransitHolderTirIdentificationLens: Lens[UserAnswers, Option[String]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderTirIdentificationLens

  val setTransitHolderContactPersonLens: Optional[UserAnswers, ContactPersonType05] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderContactPersonLens composePrism some

  val setTransitHolderAddressLens: Lens[UserAnswers, Option[AddressType17]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderAddressLens

  val setBorderMeansAnswersLens: Lens[UserAnswers, Seq[ActiveBorderTransportMeansType02]] =
    departureDataLens.composeLens(consignmentLens).composeLens(borderMeansConsignmentLens)

  val setTransportEquipmentLens: Lens[UserAnswers, Seq[TransportEquipmentType06]] =
    departureDataLens.composeLens(consignmentLens).composeLens(transportEquipmentConsignmentLens)

  val setDepartureTransportMeansAnswersLens: Lens[UserAnswers, Seq[DepartureTransportMeansType03]] =
    departureDataLens.composeLens(consignmentLens).composeLens(departureTransportMeansConsignmentLens)
}
