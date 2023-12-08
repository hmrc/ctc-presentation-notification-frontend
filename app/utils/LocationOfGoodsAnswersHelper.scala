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

import models.{LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import pages.locationOfGoods.{AuthorisationNumberPage, IdentificationPage, LocationTypePage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def locationTypeRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.locationType.checkYourAnswersLabel"),
    prefix = "locationOfGoods.locationType",
    id = Some("change-location-type"),
    call = Some(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode)),
    args = Seq.empty
  )

  def qualifierIdentificationRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.identification.checkYourAnswersLabel"),
    prefix = "locationOfGoods.identification",
    id = Some("change-qualifier-identification"),
    call = Some(controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode)),
    args = Seq.empty
  )

  def authorisationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.authorisationNumber",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.authorisationNumber),
    id = Some("change-authorisation-number")
  )

  def locationOfGoodsSection: Future[Section] = {

    val rows = for {
      locationType <- fetchValue[LocationType](
        LocationTypePage,
        checkYourAnswersReferenceDataService.getLocationType,
        userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation)
      )(userAnswers, LocationType.format).map(
        _.map(
          locType => locationTypeRow(locType.toString)
        )
      )
      qualifierIdentification <- fetchValue[LocationOfGoodsIdentification](
        IdentificationPage,
        checkYourAnswersReferenceDataService.getQualifierOfIdentification,
        userAnswers.departureData.Consignment.LocationOfGoods.map(_.qualifierOfIdentification)
      )(userAnswers, LocationOfGoodsIdentification.format).map(
        _.map(
          qualifierIdentification => qualifierIdentificationRow(qualifierIdentification.toString)
        )
      )
      rows = Seq(locationType, qualifierIdentification)
    } yield rows

    val convertedRows = rows.map(_.flatten)

    convertedRows.map {
      convertedRows =>
        Section(
          sectionTitle = messages("checkYourAnswers.locationOfGoods"),
          convertedRows
        )
    }
  }
}
