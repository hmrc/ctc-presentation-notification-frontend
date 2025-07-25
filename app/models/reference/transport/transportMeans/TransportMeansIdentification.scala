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

package models.reference.transport.transportMeans

import cats.Order
import config.FrontendAppConfig
import models.reference.RichComparison
import models.{DynamicEnumerableType, Radioable}
import play.api.libs.functional.syntax.*
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{__, Format, Json, Reads}

case class TransportMeansIdentification(`type`: String, description: String) extends Radioable[TransportMeansIdentification] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = "houseConsignment.index.departureTransportMeans.identification"

  override val code: String = `type`

}

object TransportMeansIdentification extends DynamicEnumerableType[TransportMeansIdentification] {

  def reads(config: FrontendAppConfig): Reads[TransportMeansIdentification] =
    if (config.isPhase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(TransportMeansIdentification.apply)
    } else {
      Json.reads[TransportMeansIdentification]
    }

  def queryParams(code: String)(config: FrontendAppConfig): Seq[(String, String)] = {
    val key = if (config.isPhase6Enabled) "keys" else "data.type"
    Seq(key -> code)
  }

  implicit val format: Format[TransportMeansIdentification] = Json.format[TransportMeansIdentification]

  implicit val order: Order[TransportMeansIdentification] = (x: TransportMeansIdentification, y: TransportMeansIdentification) => (x, y).compareBy(_.`type`)
}
