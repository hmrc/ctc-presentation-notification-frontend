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

import play.api.libs.json.{__, Reads}

sealed trait MessageType {

  val value: String

  override def toString: String = value
}

object MessageType {

  case object DeclarationData extends MessageType {
    override val value: String = "IE015"
  }

  case object DeclarationAmendment extends MessageType {
    override val value: String = "IE013"
  }

  case object PresentationForThePreLodgedDeclaration extends MessageType {
    override val value: String = "IE170"
  }

  case object PositiveAcknowledgement extends MessageType {
    override val value: String = "IE928"
  }

  case object AmendmentAcceptance extends MessageType {
    override val value: String = "IE004"
  }

  case object ControlDecisionNotification extends MessageType {
    override val value: String = "IE060"
  }

  case object RejectionFromOfficeOfDeparture extends MessageType {
    override val value: String = "IE056"
  }

  case class Other(value: String) extends MessageType

  implicit val reads: Reads[MessageType] =
    __.read[String].map {
      case DeclarationData.value                        => DeclarationData
      case DeclarationAmendment.value                   => DeclarationAmendment
      case PresentationForThePreLodgedDeclaration.value => PresentationForThePreLodgedDeclaration
      case PositiveAcknowledgement.value                => PositiveAcknowledgement
      case AmendmentAcceptance.value                    => AmendmentAcceptance
      case ControlDecisionNotification.value            => ControlDecisionNotification
      case RejectionFromOfficeOfDeparture.value         => RejectionFromOfficeOfDeparture
      case value                                        => Other(value)
    }
}
