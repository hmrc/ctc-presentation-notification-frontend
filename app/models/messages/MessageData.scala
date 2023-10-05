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
import play.api.libs.json.{Json, OFormat}

import scala.annotation.unused

case class MessageData(
  TransitOperation: TransitOperation,
  Authorisations: Option[Seq[Authorisation]],
  Consignment: Consignment
) {

  val isSimplified: Boolean = Authorisations.flatMap(_.find(_.`type` == C521)).isDefined

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

}

object MessageData {
  implicit val formats: OFormat[MessageData] = Json.format[MessageData]
}
