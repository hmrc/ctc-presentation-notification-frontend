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
import models.reference.BorderMode
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.transport.LimitDatePage
import pages.transport.border.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
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
    key = JsPath \ "TransitOperation" \ "limitDate",
    id = Some("change-limit-date")
  )

  def borderModeOfTransportYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderModeOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderModeOfTransport",
    key = JsPath \ "Consignment" \ "modeOfTransportAtTheBorder",
    id = Some("change-add-border-mode")
  )

  def borderModeOfTransport: Option[SummaryListRow] = getAnswerAndBuildRow[BorderMode](
    page = BorderModeOfTransportPage,
    formatAnswer = formatAsText,
    prefix = "transport.border.borderModeOfTransport",
    key = JsPath \ "Consignment" \ "modeOfTransportAtTheBorder" \ "code",
    id = Some("change-border-mode-of-transport")
  )

}
