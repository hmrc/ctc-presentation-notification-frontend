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

import models.reference.TransportMode.BorderMode
import models.{Mode, UserAnswers}
import pages.transport.border.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.Section

import java.time.LocalDate

class PresentationNotificationAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def customsOfficeDeparture: Option[SummaryListRow] =
    Option(
      buildRowWithNoChangeLink(
        prefix = "customsOfficeOfDeparture",
        answer = formatAsText(userAnswers.departureData.CustomsOfficeOfDeparture.referenceNumber)
      )
    )

  def limitDate: Option[SummaryListRow] = buildRowWithAnswer[LocalDate](
    page = LimitDatePage,
    formatAnswer = formatAsDate,
    prefix = "transport.limit.date",
    id = Some("change-limit-date")
  )

  def containerIndicator: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = ContainerIndicatorPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.containers.containerIndicator",
    id = Some("change-container-indicator")
  )

  def borderModeOfTransportYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddBorderModeOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderModeOfTransport",
    id = Some("change-add-border-mode")
  )

  def borderModeOfTransportRow: Option[SummaryListRow] = buildRowWithAnswer[BorderMode](
    page = BorderModeOfTransportPage,
    formatAnswer = formatAsText,
    prefix = "transport.border.borderModeOfTransport",
    id = Some("change-border-mode-of-transport")
  )

  def borderModeSection: Section =
    Section(
      sectionTitle = messages("transport.border.borderModeOfTransport.caption"),
      Seq(borderModeOfTransportYesNo, borderModeOfTransportRow.toList).flatten
    )

}
