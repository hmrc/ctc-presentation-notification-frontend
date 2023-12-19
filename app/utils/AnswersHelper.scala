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

import models.messages.MessageData
import models.{Mode, UserAnswers}
import pages.QuestionPage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.html.components.{Content, SummaryListRow}
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class AnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages, executionContext: ExecutionContext)
    extends SummaryListRowHelper {

  protected def lrn: String = userAnswers.lrn

  protected def getAnswerAndBuildRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    findValueInDepartureData: MessageData => Option[T],
    id: Option[String],
    args: Any*
  )(implicit rds: Reads[T]): Option[SummaryListRow] =
    for {
      answer <- userAnswers.getOrElse(page, findValueInDepartureData)
      call   <- page.route(userAnswers, departureId, mode)
    } yield buildRow(
      prefix = prefix,
      answer = formatAnswer(answer),
      id = id,
      call = call,
      args = args: _*
    )

  protected def fetchValue[T](
    page: QuestionPage[T],
    convertToReference: String => Future[T],
    valueToConvert: Option[String]
  )(implicit userAnswers: UserAnswers, rds: Reads[T]): Future[Option[T]] =
    userAnswers.get(page) match {
      case Some(value) => Future.successful(Some(value))
      case None        => valueToConvert.map(convertToReference).sequence
    }

}
