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
import pages.transport.border.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PresentationNotificationAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def limitDate: Option[SummaryListRow] = buildRowWithAnswer[LocalDate](
    page = LimitDatePage,
    optionalAnswer = userAnswers.get(LimitDatePage),
    formatAnswer = formatAsDate,
    prefix = "transport.limit.date",
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
    findValueInDepartureData = _.Consignment.isModeOfTransportDefined,
    id = Some("change-add-border-mode")
  )

  def borderModeOfTransportRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("transport.border.borderModeOfTransport.checkYourAnswersLabel"),
    prefix = "transport.border.borderModeOfTransport",
    id = Some("change-border-mode-of-transport"),
    call = Some(controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode)),
    args = Seq.empty
  )

  def borderModeSection: Future[Section] = {
    implicit val ua: UserAnswers = userAnswers
    val rows = for {

      border <- fetchValue(BorderModeOfTransportPage,
                           checkYourAnswersReferenceDataService.getBorderMode,
                           userAnswers.departureData.Consignment.modeOfTransportAtTheBorder
      )
      borderMode = border.map(
        lt => borderModeOfTransportRow(lt.toString)
      )

    } yield Seq(borderMode).flatten

    rows.map {
      borderModeOfTransport =>
        Section(
          sectionTitle = messages("transport.border.borderModeOfTransport.caption"),
          Seq(borderModeOfTransportYesNo, borderModeOfTransport).flatten
        )
    }
  }

}
