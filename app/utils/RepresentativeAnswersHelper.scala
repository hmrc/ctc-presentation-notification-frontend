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

import models.{Mode, UserAnswers}
import pages.ActingAsRepresentativePage
import pages.representative.{AddRepresentativeContactDetailsYesNoPage, EoriPage, NamePage, RepresentativePhoneNumberPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.Section

import scala.concurrent.ExecutionContext

class RepresentativeAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def actingAsRepresentative: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = ActingAsRepresentativePage,
    formatAnswer = formatAsYesOrNo,
    prefix = "actingRepresentative",
    findValueInDepartureData = data => Option(data.isRepresentativeDefined),
    id = Some("change-acting-as-representative")
  )

  def eori: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = EoriPage,
    formatAnswer = formatAsText,
    prefix = "representative.eori",
    findValueInDepartureData = _.Representative.map(_.identificationNumber),
    id = Some("change-representative-eori")
  )

  def addRepresentativeContactDetails(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddRepresentativeContactDetailsYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "addRepresentativeContactDetailsYesNo",
    findValueInDepartureData = _.Representative.map(_.isContactPersonDefined),
    id = Some("change-add-contact-details")
  )

  def name: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = NamePage,
    formatAnswer = formatAsText,
    prefix = "representative.name",
    findValueInDepartureData = _.Representative.flatMap(_.ContactPerson.map(_.name)),
    id = Some("change-representative-name")
  )

  def phoneNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = RepresentativePhoneNumberPage,
    formatAnswer = formatAsText,
    prefix = "representative.representativeTelephoneNumber",
    findValueInDepartureData = _.Representative.flatMap(_.ContactPerson.map(_.phoneNumber)),
    id = Some("change-representative-phone-number")
  )

  def representativeSection: Section = Section(
    sectionTitle = messages("checkYourAnswers.representative"),
    rows = Seq(
      actingAsRepresentative,
      eori,
      addRepresentativeContactDetails(),
      name,
      phoneNumber
    ).flatten
  )
}
