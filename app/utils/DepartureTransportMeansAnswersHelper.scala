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

package utils

import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Index, Mode, UserAnswers}
import pages.transport.departureTransportMeans._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import viewModels.Section
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.ExecutionContext

class DepartureTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode,
  transportIndex: Index
)(implicit messages: Messages, ec: ExecutionContext)
    extends AnswersHelper(userAnswers, departureId, mode) {

  implicit val ua: UserAnswers = userAnswers

  def identificationType: Option[SummaryListRow] =
    buildRowWithAnswer[TransportMeansIdentification](
      page = TransportMeansIdentificationPage(transportIndex),
      optionalAnswer = userAnswers.get(TransportMeansIdentificationPage(transportIndex)),
      formatAnswer = formatDynamicEnumAsText(_),
      prefix = "consignment.departureTransportMeans.identification",
      id = Some("change-transport-means-identification")
    )

  def identificationNumberRow: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = TransportMeansIdentificationNumberPage(transportIndex),
    optionalAnswer = userAnswers.get(TransportMeansIdentificationNumberPage(transportIndex)),
    formatAnswer = formatAsText,
    prefix = "consignment.departureTransportMeans.identificationNumber",
    id = Some("change-departure-transport-means-identification-number")
  )

  def nationality: Option[SummaryListRow] =
    buildRowWithAnswer[Nationality](
      page = TransportMeansNationalityPage(transportIndex),
      optionalAnswer = userAnswers.get(TransportMeansNationalityPage(transportIndex)),
      formatAnswer = formatAsNationality,
      prefix = "consignment.departureTransportMeans.nationality",
      id = Some("change-departure-transport-means-nationality")
    )

  def buildDepartureTransportMeansSection: Option[Section] = {

    val predicate: Boolean = userAnswers.departureData.Consignment.inlandModeOfTransport match {
      case Some(value) => value != "5"
      case None        => true
    }

    if (predicate) {

      val rows = Seq(identificationType, identificationNumberRow, nationality).flatten

        Some(
          Section(
            sectionTitle = messages("checkYourAnswers.departureTransportMeans"),
            rows = rows,
            addAnotherLink = addOrRemoveDepartureTransportsMeans()
          )
        )
      } else {
      None
    }
  }

  private def addOrRemoveDepartureTransportsMeans(): Option[Link] =
    Some(
      Link(
        id = "add-or-remove-departure-transport-means",
        text = messages("checkYourAnswers.departureTransportMeans.addOrRemove"),
        href = ""
      )
    )
}
