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

import cats.Order
import config.FrontendAppConfig
import models.{DynamicEnumerableType, Radioable}
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Format, Json, Reads}

case class SpecificCircumstanceIndicator(
  code: String,
  description: String
) extends Radioable[SpecificCircumstanceIndicator] {

  override def toString: String = s"$code - $description"

  override val messageKeyPrefix: String = "specificCircumstanceIndicator"
}

object SpecificCircumstanceIndicator extends DynamicEnumerableType[SpecificCircumstanceIndicator] {

  def reads(config: FrontendAppConfig): Reads[SpecificCircumstanceIndicator] =
    if (config.isPhase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(SpecificCircumstanceIndicator.apply)
    } else {
      Json.reads[SpecificCircumstanceIndicator]
    }

  implicit val format: Format[SpecificCircumstanceIndicator] = Json.format[SpecificCircumstanceIndicator]

  implicit val order: Order[SpecificCircumstanceIndicator] = (x: SpecificCircumstanceIndicator, y: SpecificCircumstanceIndicator) =>
    (x, y).compareBy(_.toString)
}
