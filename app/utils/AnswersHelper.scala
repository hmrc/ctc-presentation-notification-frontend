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
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.Section
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components.{Content, SummaryListRow}
import viewModels.{Link, ListItem}

class AnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig)
    extends SummaryListRowHelper {

  protected def lrn: String = userAnswers.lrn

  protected def getAnswerAndBuildRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    for {
      answer <- userAnswers.get(page)
      call   <- page.route(userAnswers, departureId, mode)
    } yield buildRow(
      prefix = prefix,
      answer = formatAnswer(answer),
      id = id,
      call = call,
      args = args: _*
    )

}
