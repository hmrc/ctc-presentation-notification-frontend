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

import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{Format, Json}
import config.Constants._

case class BorderMode(code: String, description: String) extends Radioable[BorderMode] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = BorderMode.messageKeyPrefix
}

object BorderMode extends DynamicEnumerableType[BorderMode] {

  //TODO: update this to use reference data calls
  def getDescription(value: String): BorderMode = value match {
    case Maritime => BorderMode(Maritime, "Maritime")
    case Rail     => BorderMode(Rail, "Rail")
    case Road     => BorderMode(Road, "Road")
    case Air      => BorderMode(Air, "Air")
  }
  implicit val format: Format[BorderMode] = Json.format[BorderMode]

  val messageKeyPrefix = "transport.border.borderModeOfTransport"
}
