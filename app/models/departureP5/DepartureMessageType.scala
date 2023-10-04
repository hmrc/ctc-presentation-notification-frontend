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

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait DepartureMessageType extends WithName {
  val dataPath: String
}

object DepartureMessageType extends Enumerable.Implicits {

  case object DepartureNotification extends WithName("IE015") with DepartureMessageType {
    override val dataPath: String = "CC015C"
  }

  case object AmendmentSubmitted extends WithName("IE013") with DepartureMessageType {
    override val dataPath: String = "CC013C"
  }

  case class UnknownMessageType(status: String) extends WithName(status) with DepartureMessageType {
    override val dataPath: String = status
  }

  val values: Seq[DepartureMessageType] = Seq(
    DepartureNotification,
    AmendmentSubmitted
  )

  implicit val enumerable: Enumerable[DepartureMessageType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def readsDepartureMessageType(implicit ev: Enumerable[DepartureMessageType]): Reads[DepartureMessageType] =
    Reads {
      case JsString(str) =>
        ev.withName(str)
          .map(JsSuccess(_))
          .getOrElse(
            JsSuccess(UnknownMessageType(str))
          )
      case _ =>
        JsError("error.invalid")
    }
}
