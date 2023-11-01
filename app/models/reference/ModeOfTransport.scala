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

package models.reference

import config.Constants._
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{Format, Json}

sealed trait ModeOfTransport[T] extends Radioable[T] {
  val code: String
  val description: String

  def isRail: Boolean = code == Rail
  def isAir: Boolean  = code == Air
}

case class InlandMode(code: String, description: String) extends ModeOfTransport[InlandMode] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = InlandMode.messageKeyPrefix
}

object InlandMode extends DynamicEnumerableType[InlandMode] {
  implicit val format: Format[InlandMode] = Json.format[InlandMode]

  val messageKeyPrefix = "transportMeans.inlandMode"
}

case class BorderMode(code: String, description: String) extends ModeOfTransport[BorderMode] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = BorderMode.messageKeyPrefix
}

object BorderMode extends DynamicEnumerableType[BorderMode] {
  implicit val format: Format[BorderMode] = Json.format[BorderMode]

  val messageKeyPrefix = "transportMeans.borderModeOfTransport"
}
