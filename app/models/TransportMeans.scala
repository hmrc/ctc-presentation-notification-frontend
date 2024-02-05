/*
 * Copyright 2024 HM Revenue & Customs
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

import models.reference.transport.transportMeans.TransportMeansIdentification
import play.api.i18n.Messages

case class TransportMeans(identificationType: TransportMeansIdentification, identificationNumber: Option[String]) {

  def asString(implicit messages: Messages): String = identificationNumber match {
    case Some(value) => s"${identificationType.asString} - $value"
    case None        => identificationType.asString
  }
}
