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

import cats.data.OptionT
import config.FrontendAppConfig
import models.messages.MessageData
import models.{LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import pages.locationOfGoods.{AuthorisationNumberPage, IdentificationPage, LocationTypePage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import shapeless.T
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsAnswersHelper(
                                    userAnswers: UserAnswers,
                                    departureId: String,
                                    identificationTypes: Seq[LocationOfGoodsIdentification],
                                    checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
                                    mode: Mode
                                  )(implicit messages: Messages, appConfig: FrontendAppConfig, ec: ExecutionContext, hc: HeaderCarrier)
  extends AnswersHelper(userAnswers, departureId, mode) {

  def locationTypeRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.locationType"),
    prefix = "locationOfGoods.locationType",
    id = Some("change-location-type"),
    call = None,
    args = Seq.empty
  )

  def qualifierIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[LocationOfGoodsIdentification](
    page = IdentificationPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.identification",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.map(_.qualifierOfIdentification.asQualifierIdentification),
    id = Some("change-qualifier-identification")
  )

  def authorisationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.authorisationNumber",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.authorisationNumber),
    id = Some("change-authorisation-number")
  )


  def fetchLocationType: Future[Option[LocationType]] = {
    userAnswers.get(LocationTypePage) match {
      case Some(value) => Future.successful(Some(value))
      case None =>
        userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation) match {
          case Some(value) => checkYourAnswersReferenceDataService.getLocationType(value)
        }
    }
  }

  def locationOfGoodsSection: Future[Section] = {

    val locationType = fetchLocationType.map(_.map(locType => locationTypeRow(locType.toString)))

    locationType.map {
      locationType =>
          Section(
            sectionTitle = messages("checkYourAnswers.locationOfGoods"),
            locationType.toSeq
          )
    }
  }
}
