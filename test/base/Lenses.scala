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

  private val consignmentLens: Lens[CC015CType, ConsignmentType23] =
    GenLens[CC015CType](_.Consignment)

  private val representativeLens: Lens[CC015CType, Option[RepresentativeType06]] =
    GenLens[CC015CType](_.Representative)

  private val modeOfTransportAtTheBorderConsignmentLens: Lens[ConsignmentType23, Option[String]] =
    GenLens[ConsignmentType23](_.modeOfTransportAtTheBorder)

  private val inlandModeOfTransportConsignmentLens: Lens[ConsignmentType23, Option[String]] =
    GenLens[ConsignmentType23](_.inlandModeOfTransport)

  private val containerIndicatorLens: Lens[ConsignmentType23, Option[Flag]] =
    GenLens[ConsignmentType23](_.containerIndicator)

  private val locationOfGoodsConsignmentLens: Lens[ConsignmentType23, Option[LocationOfGoodsType04]] =
    GenLens[ConsignmentType23](_.LocationOfGoods)

  private val placeOfLoadingConsignmentLens: Lens[ConsignmentType23, Option[PlaceOfLoadingType]] =
    GenLens[ConsignmentType23](_.PlaceOfLoading)

  private val departureTransportMeansConsignmentLens: Lens[ConsignmentType23, Seq[DepartureTransportMeansType01]] =
    GenLens[ConsignmentType23](_.DepartureTransportMeans)

  private val borderMeansConsignmentLens: Lens[ConsignmentType23, Seq[ActiveBorderTransportMeansType03]] =
    GenLens[ConsignmentType23](_.ActiveBorderTransportMeans)

  private val transportEquipmentConsignmentLens: Lens[ConsignmentType23, Seq[TransportEquipmentType03]] =
    GenLens[ConsignmentType23](_.TransportEquipment)

  private val addressLocationLens: Lens[LocationOfGoodsType04, Option[AddressType06]] =
    GenLens[LocationOfGoodsType04](_.Address)

  private val postAddressLocationLens: Lens[LocationOfGoodsType04, Option[PostcodeAddressType]] =
    GenLens[LocationOfGoodsType04](_.PostcodeAddress)

  private val lensContactPerson: Lens[LocationOfGoodsType04, Option[ContactPersonType01]] =
    GenLens[LocationOfGoodsType04](_.ContactPerson)

  private val lensRepresentativeContactPerson: Lens[RepresentativeType06, Option[ContactPersonType03]] =
    GenLens[RepresentativeType06](_.ContactPerson)

  private val lensAdditionalIdentifier: Lens[LocationOfGoodsType04, Option[String]] =
    GenLens[LocationOfGoodsType04](_.additionalIdentifier)

  val setModeOfTransportAtTheBorderOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.andThen(consignmentLens).andThen(modeOfTransportAtTheBorderConsignmentLens)

  val setInlandModeOfTransportOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.andThen(consignmentLens).andThen(inlandModeOfTransportConsignmentLens)

  val setContainerIndicatorOnUserAnswersLens: Lens[UserAnswers, Option[Flag]] =
    departureDataLens.andThen(consignmentLens).andThen(containerIndicatorLens)

  val setLocationOfGoodsOnUserAnswersLens: Lens[UserAnswers, Option[LocationOfGoodsType04]] =
    departureDataLens.andThen(consignmentLens).andThen(locationOfGoodsConsignmentLens)

  val setPlaceOfLoadingOnUserAnswersLens: Lens[UserAnswers, Option[PlaceOfLoadingType]] =
    departureDataLens.andThen(consignmentLens).andThen(placeOfLoadingConsignmentLens)

  val setRepresentativeOnUserAnswersLens: Lens[UserAnswers, Option[RepresentativeType06]] =
    departureDataLens andThen representativeLens

  val setAddressOnUserAnswersLens: Optional[UserAnswers, AddressType06] =
    departureDataLens andThen consignmentLens andThen locationOfGoodsConsignmentLens andThen some andThen addressLocationLens andThen some

  val setPostAddressOnUserAnswersLens: Optional[UserAnswers, PostcodeAddressType] =
    departureDataLens andThen consignmentLens andThen locationOfGoodsConsignmentLens andThen some andThen postAddressLocationLens andThen some

  val setContactPersonOnUserAnswersLens: Optional[UserAnswers, ContactPersonType01] =
    departureDataLens andThen consignmentLens andThen locationOfGoodsConsignmentLens andThen some andThen lensContactPerson andThen some

  val setRepresentativeContactPersonDetailsOnUserAnswersLens: Optional[UserAnswers, ContactPersonType03] =
    departureDataLens andThen representativeLens andThen some andThen lensRepresentativeContactPerson andThen some

  val setAdditionalIdentifierOnUserAnswersLens: Optional[UserAnswers, String] =
    departureDataLens andThen consignmentLens andThen locationOfGoodsConsignmentLens andThen some andThen lensAdditionalIdentifier andThen some

  private val customsOfficeReferenceLens: Lens[CC015CType, String] =
    GenLens[CC015CType](_.CustomsOfficeOfDeparture.referenceNumber)

  val setCustomsOfficeDepartureReferenceLens: Lens[UserAnswers, String] =
    departureDataLens andThen customsOfficeReferenceLens

  private val transitHolderLens: Lens[CC015CType, HolderOfTheTransitProcedureType23] =
    GenLens[CC015CType](_.HolderOfTheTransitProcedure)

  private val transitOperationLens: Lens[CC015CType, TransitOperationType03] =
    GenLens[CC015CType](_.TransitOperation)

  private val limitDateLens: Lens[TransitOperationType03, Option[XMLGregorianCalendar]] =
    GenLens[TransitOperationType03](_.limitDate)

  private val transitHolderEoriLens: Lens[HolderOfTheTransitProcedureType23, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType23](_.identificationNumber)

  private val transitHolderNameLens: Lens[HolderOfTheTransitProcedureType23, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType23](_.name)

  private val transitHolderTirIdentificationLens: Lens[HolderOfTheTransitProcedureType23, Option[String]] =
    GenLens[HolderOfTheTransitProcedureType23](_.TIRHolderIdentificationNumber)

  private val transitHolderContactPersonLens: Lens[HolderOfTheTransitProcedureType23, Option[ContactPersonType03]] =
    GenLens[HolderOfTheTransitProcedureType23](_.ContactPerson)

  private val transitHolderAddressLens: Lens[HolderOfTheTransitProcedureType23, Option[AddressType14]] =
    GenLens[HolderOfTheTransitProcedureType23](_.Address)

  val setTransitHolderEoriLens: Lens[UserAnswers, Option[String]] =
    departureDataLens andThen transitHolderLens andThen transitHolderEoriLens

  val setLimitDateLens: Lens[UserAnswers, Option[XMLGregorianCalendar]] =
    departureDataLens andThen transitOperationLens andThen limitDateLens

  val setTransitHolderNameLens: Lens[UserAnswers, Option[String]] =
    departureDataLens andThen transitHolderLens andThen transitHolderNameLens

  val setTransitHolderTirIdentificationLens: Lens[UserAnswers, Option[String]] =
    departureDataLens andThen transitHolderLens andThen transitHolderTirIdentificationLens

  val setTransitHolderContactPersonLens: Optional[UserAnswers, ContactPersonType03] =
    departureDataLens andThen transitHolderLens andThen transitHolderContactPersonLens andThen some

  val setTransitHolderAddressLens: Lens[UserAnswers, Option[AddressType14]] =
    departureDataLens andThen transitHolderLens andThen transitHolderAddressLens

  val setBorderMeansAnswersLens: Lens[UserAnswers, Seq[ActiveBorderTransportMeansType03]] =
    departureDataLens.andThen(consignmentLens).andThen(borderMeansConsignmentLens)

  val setTransportEquipmentLens: Lens[UserAnswers, Seq[TransportEquipmentType03]] =
    departureDataLens.andThen(consignmentLens).andThen(transportEquipmentConsignmentLens)

  val setDepartureTransportMeansAnswersLens: Lens[UserAnswers, Seq[DepartureTransportMeansType01]] =
    departureDataLens.andThen(consignmentLens).andThen(departureTransportMeansConsignmentLens)
}
