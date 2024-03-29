/*
 * Copyright 2023 HM Revenue & Customs
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

package models.messages

import cats.implicits._
import models.Constants.modeOfTransportIsAir
import models.messages.AuthorisationType.{C521, C523}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CustomsOfficeOfTransitDeclared(referenceNumber: String)

object CustomsOfficeOfTransitDeclared {
  implicit val formats: OFormat[CustomsOfficeOfTransitDeclared] = Json.format[CustomsOfficeOfTransitDeclared]
}

case class CustomsOfficeOfExitForTransitDeclared(referenceNumber: String)

object CustomsOfficeOfExitForTransitDeclared {
  implicit val formats: OFormat[CustomsOfficeOfExitForTransitDeclared] = Json.format[CustomsOfficeOfExitForTransitDeclared]
}

case class MessageData(
  CustomsOfficeOfDeparture: String,
  CustomsOfficeOfDestination: String,
  TransitOperation: TransitOperation,
  Authorisation: Option[Seq[Authorisation]],
  HolderOfTheTransitProcedure: HolderOfTheTransitProcedure,
  Representative: Option[Representative],
  CustomsOfficeOfTransitDeclared: Option[Seq[CustomsOfficeOfTransitDeclared]],
  CustomsOfficeOfExitForTransitDeclared: Option[Seq[CustomsOfficeOfExitForTransitDeclared]],
  Consignment: Consignment
) {

  val isSimplified: Boolean = Authorisation.flatMap(_.find(_.`type` == C521)).isDefined
  val hasAuthC523: Boolean  = Authorisation.flatMap(_.find(_.`type` == C523)).isDefined

  val isRepresentativeDefined: Boolean = Representative.isDefined

  def isDataCompleteSimplified: Boolean =
    List(
      TransitOperation.limitDate,
      Consignment.containerIndicator,
      Consignment.modeOfTransportAtTheBorder,
      Consignment.TransportEquipment,
      Consignment.LocationOfGoods,
      Consignment.ActiveBorderTransportMeans,
      Consignment.PlaceOfLoading
    ).sequence.isDefined

  def isDataCompleteNormal: Boolean =
    List(
      Consignment.containerIndicator,
      Consignment.modeOfTransportAtTheBorder,
      Consignment.TransportEquipment,
      Consignment.LocationOfGoods,
      Consignment.ActiveBorderTransportMeans,
      Consignment.PlaceOfLoading
    ).sequence.isDefined

  def countryOfDeparture: String = CustomsOfficeOfDeparture.take(2)

  def customsOffices: Seq[String] =
    Seq(CustomsOfficeOfDestination) ++ CustomsOfficeOfTransitDeclared
      .map(_.map(_.referenceNumber))
      .getOrElse(Seq.empty) ++
      CustomsOfficeOfExitForTransitDeclared
        .map(_.map(_.referenceNumber))
        .getOrElse(Seq.empty)

  val conveyanceRefNumberYesNoPresent: Boolean = TransitOperation.isSecurityTypeInSet && Consignment.modeOfTransportAtTheBorder == Option(modeOfTransportIsAir)
}

object MessageData {

  implicit val reads: Reads[MessageData] = (
    (__ \ "CustomsOfficeOfDeparture" \ "referenceNumber").read[String] and
      (__ \ "CustomsOfficeOfDestinationDeclared" \ "referenceNumber").read[String] and
      (__ \ "TransitOperation").read[TransitOperation] and
      (__ \ "Authorisation").readNullable[Seq[Authorisation]] and
      (__ \ "HolderOfTheTransitProcedure").read[HolderOfTheTransitProcedure] and
      (__ \ "Representative").readNullable[Representative] and
      (__ \ "CustomsOfficeOfTransitDeclared").readNullable[Seq[CustomsOfficeOfTransitDeclared]] and
      (__ \ "CustomsOfficeOfExitForTransitDeclared").readNullable[Seq[CustomsOfficeOfExitForTransitDeclared]] and
      (__ \ "Consignment").read[Consignment]
  )(MessageData.apply _)

  implicit val writes: Writes[MessageData] = (
    (__ \ "CustomsOfficeOfDeparture" \ "referenceNumber").write[String] and
      (__ \ "CustomsOfficeOfDestinationDeclared" \ "referenceNumber").write[String] and
      (__ \ "TransitOperation").write[TransitOperation] and
      (__ \ "Authorisation").writeNullable[Seq[Authorisation]] and
      (__ \ "HolderOfTheTransitProcedure").write[HolderOfTheTransitProcedure] and
      (__ \ "Representative").writeNullable[Representative] and
      (__ \ "CustomsOfficeOfTransitDeclared").writeNullable[Seq[CustomsOfficeOfTransitDeclared]] and
      (__ \ "CustomsOfficeOfExitForTransitDeclared").writeNullable[Seq[CustomsOfficeOfExitForTransitDeclared]] and
      (__ \ "Consignment").write[Consignment]
  )(unlift(MessageData.unapply))
}
