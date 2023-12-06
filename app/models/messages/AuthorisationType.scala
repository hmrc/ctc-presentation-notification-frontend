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

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait AuthorisationType

object AuthorisationType extends Enumerable.Implicits {

  case object C521 extends WithName("C521") with AuthorisationType
  case object C523 extends WithName("C523") with AuthorisationType

  case class Other(value: String) extends WithName(value) with AuthorisationType

  val values: Seq[AuthorisationType] = Seq(
    C521,
    C523
  )

  implicit val enumerable: Enumerable[AuthorisationType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def authTypeReads(implicit ev: Enumerable[AuthorisationType]): Reads[AuthorisationType] =
    Reads {
      case JsString(str) =>
        ev.withName(str)
          .map(JsSuccess(_))
          .getOrElse(
            JsSuccess(Other(str))
          )
      case value =>
        JsError(s"AuthorisationType: could not read: $value as JsString")
    }
}
