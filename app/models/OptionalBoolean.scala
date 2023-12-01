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

package models

import play.api.libs.json._

case class OptionalBoolean(value: Option[Boolean]) {

  override def toString: String = value.map(_.toString).getOrElse("maybe")
}

object OptionalBoolean {

  def yes: OptionalBoolean   = OptionalBoolean(Some(true))
  def no: OptionalBoolean    = OptionalBoolean(Some(false))
  def maybe: OptionalBoolean = OptionalBoolean(None)

  implicit val reads: Reads[OptionalBoolean] = Reads {
    case boolean: JsBoolean => JsSuccess(OptionalBoolean(Some(boolean.value)))
    case JsNull             => JsSuccess(OptionalBoolean(None))
    case x                  => JsError(s"$x is not a valid OptionalBoolean")
  }

  implicit val writes: Writes[OptionalBoolean] = Writes {
    case OptionalBoolean(Some(value)) => JsBoolean(value)
    case _                            => JsNull
  }

  implicit val format: Format[OptionalBoolean] = Format(reads, writes)
}
