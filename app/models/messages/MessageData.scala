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
import models.messages.AuthorisationType.C521
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class MessageData(
  CustomsOfficeOfDeparture: String,
  CustomsOfficeOfDestination: String,
  TransitOperation: TransitOperation,
  Authorisation: Option[Seq[Authorisation]],
  Consignment: Consignment
) {

  val isSimplified: Boolean = Authorisation.flatMap(_.find(_.`type` == C521)).isDefined

  def isDataComplete: Boolean =
    List(
      TransitOperation.limitDate,
      Consignment.containerIndicator,
      Consignment.modeOfTransportAtTheBorder,
      Consignment.TransportEquipment,
      Consignment.LocationOfGoods,
      Consignment.ActiveBorderTransportMeans,
      Consignment.PlaceOfLoading
    ).sequence.isDefined

  def countryOfDeparture: String = CustomsOfficeOfDeparture.take(2)

}

object MessageData {

  implicit val reads: Reads[MessageData] = (
    (__ \ "CustomsOfficeOfDeparture" \ "referenceNumber").read[String] and
      (__ \ "CustomsOfficeOfDestinationDeclared" \ "referenceNumber").read[String] and
      (__ \ "TransitOperation").read[TransitOperation] and
      (__ \ "Authorisation").readNullable[Seq[Authorisation]] and
      (__ \ "Consignment").read[Consignment]
  )(MessageData.apply _)

  implicit val writes: Writes[MessageData] = (
    (__ \ "CustomsOfficeOfDeparture" \ "referenceNumber").write[String] and
      (__ \ "CustomsOfficeOfDestinationDeclared" \ "referenceNumber").write[String] and
      (__ \ "TransitOperation").write[TransitOperation] and
      (__ \ "Authorisation").writeNullable[Seq[Authorisation]] and
      (__ \ "Consignment").write[Consignment]
  )(unlift(MessageData.unapply))
}
