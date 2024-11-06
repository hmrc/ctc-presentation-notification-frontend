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

package models.departureP5

import models.WithName
import play.api.libs.json.{__, Reads}

sealed trait MessageType

object MessageType {

  case object DeclarationData extends MessageType

  case object DeclarationAmendment extends MessageType

  case object PresentationForThePreLodgedDeclaration extends MessageType

  case object PositiveAcknowledgement extends MessageType

  case object AmendmentAcceptance extends MessageType

  case object ControlDecisionNotification extends MessageType

  case class Other(status: String) extends WithName(status) with MessageType

  implicit val reads: Reads[MessageType] =
    __.read[String].map {
      case "IE015" => DeclarationData
      case "IE013" => DeclarationAmendment
      case "IE170" => PresentationForThePreLodgedDeclaration
      case "IE928" => PositiveAcknowledgement
      case "IE004" => AmendmentAcceptance
      case "IE060" => ControlDecisionNotification
      case x       => Other(x)
    }
}
