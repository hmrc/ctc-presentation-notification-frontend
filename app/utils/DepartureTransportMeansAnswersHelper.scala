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
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Mode, UserAnswers}
import pages.transport.border.active.IdentificationNumberPage
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage, TransportMeansNationalityPage}
import pages.transport.{AddInlandModeOfTransportYesNoPage, InlandModePage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

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
          id = Some("change-transport-inland-mode")
        )
    }

  def identificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = TransportMeansIdentificationNumberPage,
    formatAnswer = formatAsText,
    prefix = "consignment.departureTransportMeans.identificationNumber",
    findValueInDepartureData = _.Consignment.DepartureTransportMeans.flatMap(_.identificationNumber),
    id = Some("change-departure-transport-means-identification-number")
  )

//  def nationality: Option[SummaryListRow] = getAnswerAndBuildRow[Nationality](
//    page = TransportMeansNationalityPage,
//    formatAnswer = formatAsText,
//    prefix = "consignment.departureTransportMeans.identificationNumber",
//    findValueInDepartureData = _.Consignment.DepartureTransportMeans.flatMap(_.identificationNumber),
//    id = Some("change-departure-transport-means-identification-number")
//  )

//  def buildInlandModeSection: Future[Option[Section]] =
//    if (!userAnswers.departureData.TransitOperation.reducedDatasetIndicator.asBoolean) {
//      val inlandModeYesNoRow  = inlandModeOfTransportYesNo
//      val inlandModeFutureRow = inlandMode
//
//      inlandModeFutureRow.map {
//        inlandModeRow =>
//          val rows = Seq(inlandModeYesNoRow, inlandModeRow).flatten
//
//          Some(
//            Section(
//              sectionTitle = messages("checkYourAnswers.inlandMode"),
//              rows = rows
//            )
//          )
//      }
//    } else {
//      successful(None)
//    }
}
