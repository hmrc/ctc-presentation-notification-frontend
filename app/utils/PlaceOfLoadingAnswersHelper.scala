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

import models.reference.Country
import models.useranswers.PlaceOfLoadingUA
import models.{Mode, UserAnswers}
import pages.loading._
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class PlaceOfLoadingAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def addUnlocodeYesNo(answer: Option[Boolean]): Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddUnLocodeYesNoPage,
    optionalAnswer = answer,
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addUnLocodeYesNo",
    id = Some("change-add-unlocode")
  )

  def unlocode(answer: Option[String]): Option[SummaryListRow] = buildRowWithAnswer[String](
    page = UnLocodePage,
    optionalAnswer = answer,
    formatAnswer = formatAsText,
    prefix = "loading.unLocode",
    id = Some("change-unlocode")
  )

  def addExtraInformationYesNo(answer: Option[Boolean]): Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddExtraInformationYesNoPage,
    optionalAnswer = answer,
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addExtraInformationYesNo",
    id = Some("change-add-extra-information")
  )

  def country(answer: Option[Country]): Option[SummaryListRow] = buildRowWithAnswer[Country](
    page = CountryPage,
    optionalAnswer = answer,
    formatAnswer = formatAsCountry,
    prefix = "loading.country",
    id = Some("change-country")
  )

  def location(answer: Option[String]): Option[SummaryListRow] = buildRowWithAnswer[String](
    page = LocationPage,
    optionalAnswer = answer,
    formatAnswer = formatAsText,
    prefix = "loading.location",
    id = Some("change-location")
  )

  def placeOfLoadingSection: Future[Section] = {
    val answers = PlaceOfLoadingUA(userAnswers, userAnswers.departureData.Consignment.PlaceOfLoading, checkYourAnswersReferenceDataService)
    for {
      countryAnswer <- answers.country
    } yield {
      val rows = Seq(
        addUnlocodeYesNo(answers.addUnlocodeYesNo),
        unlocode(answers.unlocode),
        addExtraInformationYesNo(answers.addExtraInformationYesNo),
        country(countryAnswer),
        location(answers.location)
      ).flatten

      Section(
        sectionTitle = messages("checkYourAnswers.placeOfLoading"),
        rows
      )
    }
  }
}
