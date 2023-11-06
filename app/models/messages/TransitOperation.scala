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

import config.Constants.{EntryAndExitSummaryDeclarationSecurityDetails, EntrySummaryDeclarationSecurityDetails, ExitSummaryDeclarationSecurityDetails}
import play.api.libs.json.{Json, OFormat}

case class TransitOperation(limitDate: Option[String], security: String) {

  def isSecurityTypeInSet: Boolean = {
    val allowed = Set(EntrySummaryDeclarationSecurityDetails, ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails)
    allowed.contains(security)
  }

}

object TransitOperation {
  implicit val format: OFormat[TransitOperation] = Json.format[TransitOperation]
}
