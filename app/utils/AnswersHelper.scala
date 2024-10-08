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

import models.{Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.QuestionPage
import pages.sections.Section
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Reads}
import uk.gov.hmrc.govukfrontend.views.html.components.{Content, SummaryListRow}
import viewModels.Link

class AnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages)
    extends SummaryListRowHelper {

  protected def lrn: String = userAnswers.lrn

  def getAnswersAndBuildSectionRows(section: Section[JsArray])(f: Index => Option[SummaryListRow]): Seq[SummaryListRow] =
    userAnswers
      .get(section)
      .mapWithIndex {
        (_, index) => f(index)
      }

  protected def buildRowWithAnswer[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    args: Any*
  )(implicit reads: Reads[T]): Option[SummaryListRow] =
    for {
      answer <- userAnswers.get(page)
      call   <- page.route(userAnswers, departureId, mode)
    } yield buildRow(
      prefix = prefix,
      answer = formatAnswer(answer),
      id = id,
      call = call,
      args = args*
    )

  protected def buildLink[T](section: Section[JsArray], doesSectionExistInDepartureData: Boolean)(link: => Link): Option[Link] =
    if (userAnswers.get(section).isDefined || doesSectionExistInDepartureData) Some(link) else None

}
