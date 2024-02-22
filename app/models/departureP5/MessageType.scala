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

sealed trait MessageType {
  val dataPath: String
}

object MessageType {

  case object DepartureNotification extends MessageType {
    override val dataPath: String = "CC015C"
  }

  case object AmendmentSubmitted extends MessageType {
    override val dataPath: String = "CC013C"
  }

  case class Other(status: String) extends WithName(status) with MessageType {
    override val dataPath: String = status
  }

  implicit val reads: Reads[MessageType] =
    __.read[String].map {
      case "IE015" => DepartureNotification
      case "IE013" => AmendmentSubmitted
      case x       => Other(x)
    }
}
