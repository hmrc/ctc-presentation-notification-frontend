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

import models.{DynamicAddress, Radioable}
import models.reference.{Country, Item, Nationality}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._

import java.time.LocalDate

private[utils] class SummaryListRowHelper(implicit messages: Messages) {

  protected def formatAsYesOrNo(answer: Boolean): Content =
    formatAsYesOrNo(answer, "site")

  protected def formatAsYesOrNo(answer: Boolean, prefix: String): Content =
    if (answer) {
      messages(s"$prefix.yes").toText
    } else {
      messages(s"$prefix.no").toText
    }

  def formatAsDate(answer: LocalDate): Content = answer.formatAsString.toText

  protected def formatAsText[T](answer: T): Content = s"$answer".toText

  protected def formatAsItem(item: Item): Content = item.description.toText

  protected def formatAsCountry(country: Country): Content = country.description.toText

  protected def formatAsNationality(nationality: Nationality): Content = nationality.description.toText

  protected def formatDynamicEnumAsString[T <: Radioable[T]](answer: T): String = answer.asString

  protected def formatAsDynamicAddress(address: DynamicAddress): Content =
    HtmlContent(address.toString)

  protected def formatDynamicEnumAsText[T <: Radioable[T]](answer: T): Content = formatDynamicEnumAsString(answer).toText

  protected def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): SummaryListRow =
    buildSimpleRow(
      prefix = prefix,
      label = messages(s"$prefix.checkYourAnswersLabel", args*),
      answer = answer,
      id = id,
      call = Some(call),
      args = args*
    )

  protected def buildRowWithNoChangeLink(
    prefix: String,
    answer: Content,
    args: Any*
  ): SummaryListRow =
    buildSimpleRow(
      prefix = prefix,
      label = messages(s"$prefix.checkYourAnswersLabel", args*),
      answer = answer,
      id = None,
      call = None
    )

  protected def buildSimpleRow(
    prefix: String,
    label: String,
    answer: Content,
    id: Option[String],
    call: Option[Call],
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = label.toKey,
      value = Value(answer),
      actions = call.map {
        route =>
          Actions(
            items = List(
              ActionItem(
                content = messages("site.edit").toText,
                href = route.url,
                visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args*)),
                attributes = id.fold[Map[String, String]](Map.empty)(
                  id => Map("id" -> id)
                )
              )
            )
          )
      }
    )

}
