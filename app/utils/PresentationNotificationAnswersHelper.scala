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

import config.FrontendAppConfig
import models.reference.Country
import models.{Mode, UserAnswers}
import pages.loading._
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

import java.time.LocalDate

class PresentationNotificationAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def limitDate: Option[SummaryListRow] = getAnswerAndBuildRow[LocalDate](
    page = LimitDatePage,
    formatAnswer = formatAsDate,
    prefix = "transport.limit.date",
    findValueInDepartureData = _.TransitOperation.limitDate.map(_.asLocalDate),
    id = Some("change-limit-date")
  )

  def containerIndicator: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = ContainerIndicatorPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.containers.containerIndicator",
    findValueInDepartureData = _.Consignment.containerIndicator.map(_.asBoolean),
    id = Some("change-container-indicator")
  )

  def addUnlocodeYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddUnLocodeYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addUnLocodeYesNo",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.map(_.isUnlocodePresent),
    id = Some("change-add-unlocode")
  )

  def unlocode: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = UnLocodePage,
    formatAnswer = formatAsText,
    prefix = "loading.unLocode",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.flatMap(_.UNLocode),
    id = Some("change-unlocode")
  )

  def addExtraInformationYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddExtraInformationYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loading.addExtraInformationYesNo",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.map(_.isAdditionalInformationPresent),
    id = Some("change-add-extra-information")
  )

  def country: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "loading.country",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.flatMap(_.country.map(_.asCountry)),
    id = Some("change-country")
  )

  def location: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = LocationPage,
    formatAnswer = formatAsText,
    prefix = "loading.location",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.flatMap(_.location),
    id = Some("change-location")
  )

}
