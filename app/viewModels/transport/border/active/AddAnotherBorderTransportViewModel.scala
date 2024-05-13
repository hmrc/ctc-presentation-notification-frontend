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

package viewModels.transport.border.active

import config.Constants._
import config.FrontendAppConfig
import models.{Index, Mode, RichCC015CType, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.Content
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherBorderTransportViewModel(listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "transport.border.active.addAnotherBorderTransport"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxActiveBorderTransports

  def hint(implicit messages: Messages): Content = messages(s"$prefix.hint").toText
}

object AddAnotherBorderTransportViewModel {

  class AddAnotherBorderTransportViewModelProvider() {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode)(implicit messages: Messages): AddAnotherBorderTransportViewModel = {
      val isSectionMandatory = userAnswers.get(BorderModeOfTransportPage).exists(_.code != Mail) ||
        userAnswers.departureData.hasSecurity ||
        userAnswers.departureData.CustomsOfficeOfTransitDeclared.nonEmpty

      val listItems = userAnswers
        .get(BorderActiveListSection)
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val index = Index(i)
            ((userAnswers.get(IdentificationPage(index)), userAnswers.get(IdentificationNumberPage(index))) match {
              case (Some(identification), Some(identificationNumber)) => Some(s"${identification.asString} - $identificationNumber")
              case (Some(identification), None)                       => Some(identification.asString)
              case (None, Some(identificationNumber))                 => Some(identificationNumber)
              case _                                                  => None
            }).map {
              name =>
                ListItem(
                  name = name,
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, index).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, index).url)
                )
            }
        }
        .toSeq
        .checkRemoveLinks(isSectionMandatory)

      new AddAnotherBorderTransportViewModel(
        listItems,
        onSubmitCall = controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController.onSubmit(departureId, mode)
      )
    }
  }
}
