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

package models.removable

import models.reference.transport.border.active.Identification
import models.{Index, UserAnswers}
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads

case class TransportMeans(index: Index, identification: Identification, identificationNumber: Option[String]) {

  def forRemoveDisplay: Option[String] = identificationNumber match {
    case Some(number) => Some(s"$identification - $number")
    case _            => Some(identification.toString)

  }
}

object TransportMeans {

  def apply(userAnswers: UserAnswers, transportMeansIndex: Index): Option[TransportMeans] = {

    implicit val reads: Reads[TransportMeans] = (
      IdentificationPage(transportMeansIndex).path.read[Identification] and
        IdentificationNumberPage(transportMeansIndex).path.readNullable[String]
    ).apply {
      (identifier, identificationNumber) => TransportMeans(transportMeansIndex, identifier, identificationNumber)
    }
    userAnswers.data.asOpt[TransportMeans]
  }
}
