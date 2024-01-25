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

import models.reference.TransportMode.InlandMode
import models.{Mode, UserAnswers}
import pages.transport.{AddInlandModeOfTransportYesNoPage, InlandModePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import viewModels.Section

import scala.concurrent.ExecutionContext

class InlandModeAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext)
    extends AnswersHelper(userAnswers, departureId, mode) {

  implicit val ua: UserAnswers = userAnswers

  def inlandModeOfTransportYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddInlandModeOfTransportYesNoPage,
    optionalAnswer = userAnswers.get(AddInlandModeOfTransportYesNoPage),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.addInlandModeOfTransport",
    id = Some("change-add-inland-mode-of-transport")
  )

  def inlandMode: Option[SummaryListRow] =
    buildRowWithAnswer[InlandMode](
      page = InlandModePage,
      optionalAnswer = userAnswers.get(InlandModePage),
      formatAnswer = formatDynamicEnumAsText(_),
      prefix = "transport.inlandModeOfTransport",
      id = Some("change-transport-inland-mode")
    )

  def buildInlandModeSection: Option[Section] =
    if (!userAnswers.departureData.TransitOperation.reducedDatasetIndicator.asBoolean) {
      val inlandModeYesNoRow = inlandModeOfTransportYesNo
      val inlandModeRow      = inlandMode

      val rows = Seq(inlandModeYesNoRow, inlandModeRow).flatten

      Some(
        Section(
          sectionTitle = messages("checkYourAnswers.inlandMode"),
          rows = rows
        )
      )

    } else {
      None
    }
}
