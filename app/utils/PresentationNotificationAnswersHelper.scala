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
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
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

  def borderModeOfTransportYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderModeOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderModeOfTransport",
    findValueInDepartureData = _.Consignment.isTransportDefined,
    id = Some("change-add-border-mode")
  )

  def borderModeOfTransport: Option[SummaryListRow] = getAnswerAndBuildRow[BorderMode](
    page = BorderModeOfTransportPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "transport.border.borderModeOfTransport",
    findValueInDepartureData = message =>
      userAnswers.get(AddBorderModeOfTransportYesNoPage).flatMap {
        case true  => message.Consignment.modeOfTransportAtTheBorder.map(_.asBorderMode)
        case false => None
      },
    id = Some("change-border-mode-of-transport")
  )

}
