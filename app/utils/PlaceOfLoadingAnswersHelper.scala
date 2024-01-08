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

import models.messages.PlaceOfLoading
import models.reference.{Country, CountryCode}
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

  def countryTypeRow(answer: String): Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "loading.country",
    findValueInDepartureData = message =>
      message.Consignment.PlaceOfLoading.flatMap(
        _.country.map(
          y => Country(CountryCode(y), answer)
        )
      ),
    id = Some("change-country")
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

  def location: Option[SummaryListRow] = getModelAndBuildRow[PlaceOfLoading, String](
    getValueFromModel = _.location,
    page = LocationPage,
    formatAnswer = formatAsText,
    prefix = "loading.location",
    findValueInDepartureData = _.Consignment.PlaceOfLoading.flatMap(_.location),
    id = Some("change-location")
  )(PlaceOfLoading.userAnswersReads)

  def placeOfLoadingSection: Future[Section] = {
    implicit val ua: UserAnswers = userAnswers

    val rows = for {

      country <- fetchValue[Country](
        CountryPage,
        checkYourAnswersReferenceDataService.getCountry,
        userAnswers.departureData.Consignment.LocationOfGoods.flatMap(_.Address.map(_.country))
      )
      countryRow = country.flatMap(
        x => countryTypeRow(x.description)
      )

      rowAcc = Seq(
        addUnlocodeYesNo,
        unlocode,
        addExtraInformationYesNo,
        countryRow,
        location
      ).flatten

    } yield rowAcc

    rows.map(
      convertedRows =>
        Section(
          sectionTitle = messages("checkYourAnswers.placeOfLoading"),
          convertedRows
        )
    )

  }

}
