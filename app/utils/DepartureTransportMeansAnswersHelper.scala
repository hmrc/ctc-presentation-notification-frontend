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
import models.{Mode, UserAnswers}
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans._
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  implicit val ua: UserAnswers = userAnswers

  def identificationType: Future[Option[SummaryListRow]] =
    fetchValue[TransportMeansIdentification](
      page = TransportMeansIdentificationPage,
      valueFromDepartureData = userAnswers.departureData.Consignment.DepartureTransportMeans.flatMap(_.typeOfIdentification),
      refDataLookup = checkYourAnswersReferenceDataService.getMeansOfTransportIdentificationType
    ).map {
      identificationType =>
        buildRowWithAnswer[TransportMeansIdentification](
          page = TransportMeansIdentificationPage,
          optionalAnswer = identificationType,
          formatAnswer = formatDynamicEnumAsText(_),
          prefix = "consignment.departureTransportMeans.identification",
          id = Some("change-transport-means-identification")
        )
    }

  def identificationNumberRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = TransportMeansIdentificationNumberPage,
    formatAnswer = formatAsText,
    prefix = "consignment.departureTransportMeans.identificationNumber",
    findValueInDepartureData = _.Consignment.DepartureTransportMeans.flatMap(_.identificationNumber),
    id = Some("change-departure-transport-means-identification-number")
  )

  def nationality: Future[Option[SummaryListRow]] =
    fetchValue[Nationality](
      page = TransportMeansNationalityPage,
      valueFromDepartureData = userAnswers.departureData.Consignment.DepartureTransportMeans.flatMap(_.nationality),
      refDataLookup = checkYourAnswersReferenceDataService.getNationality
    ).map {
      nationality =>
        buildRowWithAnswer[Nationality](
          page = TransportMeansNationalityPage,
          optionalAnswer = nationality,
          formatAnswer = formatAsText,
          prefix = "consignment.departureTransportMeans.nationality",
          id = Some("change-departure-transport-means-nationality")
        )
    }

  def buildDepartureTransportMeansSection: Future[Option[Section]] = {

    val inlandModeIE15: Option[String]  = userAnswers.departureData.Consignment.inlandModeOfTransport
    val inlandModeIE170: Option[String] = userAnswers.get(InlandModePage).map(_.code)

    val predicate: Boolean = (inlandModeIE15, inlandModeIE170) match {
      case (_, Some(inlandModeIE170)) => inlandModeIE170 != "5"
      case (Some(inlandModeIE15), _)  => inlandModeIE15 != "5"
      case (None, None)               => true
    }

    if (predicate) {

      for {
        identificationTypeRow   <- identificationType
        identificationNumberRow <- successful(identificationNumberRow)
        nationalityRow          <- nationality
      } yield {
        val rows = Seq(identificationTypeRow, identificationNumberRow, nationalityRow).flatten

        Some(
          Section(
            sectionTitle = messages("checkYourAnswers.departureTransportMeans"),
            rows = rows,
            addAnotherLink = addOrRemoveDepartureTransportsMeans()
          )
        )
      }
    } else {
      successful(None)
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
