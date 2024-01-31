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

package viewModels.transport.departureTransportMeans

import config.Constants.Mail
import config.FrontendAppConfig
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages.sections.transport.departureTransportMeans.TransportMeansListSection
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage}
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherTransportMeansViewModel(userAnswers: UserAnswers, listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "consignment.departureTransportMeans.addAnotherTransportMeans"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxTransportMeans

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")

  def maxLimitWarningHint1(implicit messages: Messages): String = messages(s"$prefix.maxLimit.hint1")

  def maxLimitWarningHint2(implicit messages: Messages): String = messages(s"$prefix.maxLimit.hint2")

}

object AddAnotherTransportMeansViewModel {

  class AddAnotherTransportMeansViewModelProvider() {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode)(implicit messages: Messages): AddAnotherTransportMeansViewModel = {

      val isSectionMandatory = userAnswers.get(InlandModePage).exists(_.code != Mail)

      val listItems = userAnswers
        .get(TransportMeansListSection)
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val index = Index(i)
            ((userAnswers.get(TransportMeansIdentificationPage(index)), userAnswers.get(TransportMeansIdentificationNumberPage(index))) match {
              case (Some(identification), Some(identificationNumber)) => Some(s"${identification.asString} - $identificationNumber")
              case (Some(identification), None)                       => Some(identification.asString)
              case (None, Some(identificationNumber))                 => Some(identificationNumber)
              case _                                                  => None
            }).map {
              name =>
                ListItem(
                  name = name,
                  changeUrl =
                    controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, index).url,
                  removeUrl = Some(
                    controllers.transport.departureTransportMeans.routes.RemoveDepartureTransportMeansYesNoController.onPageLoad(departureId, mode, index).url
                  )
                )
            }
        }
        .toSeq
        .checkRemoveLinks(isSectionMandatory)

      new AddAnotherTransportMeansViewModel(
        userAnswers,
        listItems,
        onSubmitCall = controllers.transport.departureTransportMeans.routes.AddAnotherTransportMeansController.onSubmit(departureId, mode)
      )
    }
  }
}
