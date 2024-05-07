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

import models.UserAnswers
import models.messages._
import monocle.macros.GenLens
import monocle.std.option._
import monocle.{Lens, Optional}

trait Lenses {

  private val departureDataLens: Lens[UserAnswers, MessageData]             = GenLens[UserAnswers](_.departureData)
  private val consignmentLens: Lens[MessageData, Consignment]               = GenLens[MessageData](_.Consignment)
  private val representativeLens: Lens[MessageData, Option[Representative]] = GenLens[MessageData](_.Representative)

  private val modeOfTransportAtTheBorderConsignmentLens: Lens[Consignment, Option[String]] =
    GenLens[Consignment](_.modeOfTransportAtTheBorder)

  private val inlandModeOfTransportConsignmentLens: Lens[Consignment, Option[String]] =
    GenLens[Consignment](_.inlandModeOfTransport)

  private val locationOfGoodsConsignmentLens: Lens[Consignment, Option[LocationOfGoods]] =
    GenLens[Consignment](_.LocationOfGoods)

  private val placeOfLoadingConsignmentLens: Lens[Consignment, Option[PlaceOfLoading]] =
    GenLens[Consignment](_.PlaceOfLoading)

  private val departureTransportMeansConsignmentLens: Lens[Consignment, Option[Seq[DepartureTransportMeans]]] =
    GenLens[Consignment](_.DepartureTransportMeans)

  private val borderMeansConsignmentLens: Lens[Consignment, Option[Seq[ActiveBorderTransportMeans]]] =
    GenLens[Consignment](_.ActiveBorderTransportMeans)

  private val transportEquipmentConsignmentLens: Lens[Consignment, Option[List[TransportEquipment]]] =
    GenLens[Consignment](_.TransportEquipment)

  private val addressLocationLens: Lens[LocationOfGoods, Option[Address]] =
    GenLens[LocationOfGoods](_.Address)

  private val postAddressLocationLens: Lens[LocationOfGoods, Option[PostcodeAddress]] =
    GenLens[LocationOfGoods](_.PostcodeAddress)

  private val lensContactPerson: Lens[LocationOfGoods, Option[ContactPerson]] = GenLens[LocationOfGoods](_.ContactPerson)

  private val lensRepresentativeContactPerson: Lens[Representative, Option[ContactPerson]] = GenLens[Representative](_.ContactPerson)

  private val lensAdditionalIdentifier: Lens[LocationOfGoods, Option[String]] = GenLens[LocationOfGoods](_.additionalIdentifier)

  val setModeOfTransportAtTheBorderOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.composeLens(consignmentLens).composeLens(modeOfTransportAtTheBorderConsignmentLens)

  val setInlandModeOfTransportOnUserAnswersLens: Lens[UserAnswers, Option[String]] =
    departureDataLens.composeLens(consignmentLens).composeLens(inlandModeOfTransportConsignmentLens)

  val setLocationOfGoodsOnUserAnswersLens: Lens[UserAnswers, Option[LocationOfGoods]] =
    departureDataLens.composeLens(consignmentLens).composeLens(locationOfGoodsConsignmentLens)

  val setPlaceOfLoadingOnUserAnswersLens: Lens[UserAnswers, Option[PlaceOfLoading]] =
    departureDataLens.composeLens(consignmentLens).composeLens(placeOfLoadingConsignmentLens)

  val setRepresentativeOnUserAnswersLens: Lens[UserAnswers, Option[Representative]] =
    departureDataLens composeLens representativeLens

  val setAddressOnUserAnswersLens: Optional[UserAnswers, Address] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens addressLocationLens composePrism some

  val setPostAddressOnUserAnswersLens: Optional[UserAnswers, PostcodeAddress] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens postAddressLocationLens composePrism some

  val setContactPersonOnUserAnswersLens: Optional[UserAnswers, ContactPerson] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens lensContactPerson composePrism some

  val setRepresentativeContactPersonDetailsOnUserAnswersLens: Optional[UserAnswers, ContactPerson] =
    departureDataLens composeLens representativeLens composePrism some composeLens lensRepresentativeContactPerson composePrism some

  val setAdditionalIdentifierOnUserAnswersLens: Optional[UserAnswers, String] =
    departureDataLens composeLens consignmentLens composeLens locationOfGoodsConsignmentLens composePrism some composeLens lensAdditionalIdentifier composePrism some

  private val customsOfficeReferenceLens: Lens[MessageData, String] = GenLens[MessageData](_.CustomsOfficeOfDeparture)

  val setCustomsOfficeDepartureReferenceLens: Lens[UserAnswers, String] = departureDataLens composeLens customsOfficeReferenceLens

  private val transitHolderLens: Lens[MessageData, HolderOfTheTransitProcedure]        = GenLens[MessageData](_.HolderOfTheTransitProcedure)
  private val transitHolderEoriLens: Lens[HolderOfTheTransitProcedure, Option[String]] = GenLens[HolderOfTheTransitProcedure](_.identificationNumber)
  private val transitHolderNameLens: Lens[HolderOfTheTransitProcedure, Option[String]] = GenLens[HolderOfTheTransitProcedure](_.name)

  private val transitHolderTirIdentificationLens: Lens[HolderOfTheTransitProcedure, Option[String]] =
    GenLens[HolderOfTheTransitProcedure](_.TIRHolderIdentificationNumber)
  private val transitHolderContactPersonLens: Lens[HolderOfTheTransitProcedure, Option[ContactPerson]] = GenLens[HolderOfTheTransitProcedure](_.ContactPerson)
  private val transitHolderAddressLens: Lens[HolderOfTheTransitProcedure, Option[Address]]             = GenLens[HolderOfTheTransitProcedure](_.Address)

  val setTransitHolderEoriLens: Lens[UserAnswers, Option[String]] = departureDataLens composeLens transitHolderLens composeLens transitHolderEoriLens

  val setTransitHolderNameLens: Lens[UserAnswers, Option[String]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderNameLens

  val setTransitHolderTirIdentificationLens: Lens[UserAnswers, Option[String]] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderTirIdentificationLens

  val setTransitHolderContactPersonLens: Optional[UserAnswers, ContactPerson] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderContactPersonLens composePrism some

  val setTransitHolderAddressLens: Optional[UserAnswers, Address] =
    departureDataLens composeLens transitHolderLens composeLens transitHolderAddressLens composePrism some

  val setBorderMeansAnswersLens: Lens[UserAnswers, Option[Seq[ActiveBorderTransportMeans]]] =
    departureDataLens.composeLens(consignmentLens).composeLens(borderMeansConsignmentLens)

  val setTransportEquipmentLens: Lens[UserAnswers, Option[List[TransportEquipment]]] =
    departureDataLens.composeLens(consignmentLens).composeLens(transportEquipmentConsignmentLens)

  val setDepartureTransportMeansAnswersLens: Lens[UserAnswers, Option[Seq[DepartureTransportMeans]]] =
    departureDataLens.composeLens(consignmentLens).composeLens(departureTransportMeansConsignmentLens)
}
