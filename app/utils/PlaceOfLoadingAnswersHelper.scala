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
import models.{Mode, UserAnswers}
import pages.loading._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class PlaceOfLoadingAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def countryTypeRow: Option[SummaryListRow] = buildRowWithAnswer[Country](
    page = CountryPage,
    optionalAnswer = userAnswers.get(CountryPage),
    formatAnswer = formatAsCountry,
    prefix = "loading.country",
    id = Some("change-country")
  )

  def addUnlocodeYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddUnLocodeYesNoPage,
    optionalAnswer = userAnswers.get(AddUnLocodeYesNoPage),
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addUnLocodeYesNo",
    id = Some("change-add-unlocode")
  )

  def unlocode: Option[SummaryListRow] = {
    val code = buildRowWithAnswer[String](
      page = UnLocodePage,
      optionalAnswer = userAnswers.get(UnLocodePage),
      formatAnswer = formatAsText,
      prefix = "loading.unLocode",
      id = Some("change-unlocode")
    )
    code
  }

  def addExtraInformationYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddExtraInformationYesNoPage,
    optionalAnswer = userAnswers.get(AddExtraInformationYesNoPage),
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addExtraInformationYesNo",
    id = Some("change-add-extra-information")
  )

  def location: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = LocationPage,
    optionalAnswer = userAnswers.get(LocationPage),
    formatAnswer = formatAsText,
    prefix = "loading.location",
    id = Some("change-location")
  )

  def placeOfLoadingSection: Future[Section] = {
    val rows = Seq(
      addUnlocodeYesNo,
      unlocode,
      if (unlocode.isDefined) addExtraInformationYesNo else None,
      countryTypeRow,
      location
    ).flatten

    Future.successful(
      Section(
        sectionTitle = messages("checkYourAnswers.placeOfLoading"),
        rows
      )
    )

  }

}
