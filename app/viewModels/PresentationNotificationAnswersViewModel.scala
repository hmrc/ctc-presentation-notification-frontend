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

package viewModels

import config.FrontendAppConfig
import models.{CheckMode, Index, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Json}
import utils.{ActiveBorderTransportMeansAnswersHelper, PresentationNotificationAnswersHelper}
import viewModels.transport.border.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

import javax.inject.Inject

case class PresentationNotificationAnswersViewModel(sections: Seq[Section])

object PresentationNotificationAnswersViewModel {

  class PresentationNotificationAnswersViewModelProvider @Inject() (
    activeBorderAnswersViewModelProvider: ActiveBorderAnswersViewModelProvider
  )(implicit
    val config: FrontendAppConfig
  ) {

    // scalastyle:off method.length
    def apply(userAnswers: UserAnswers, departureId: String)(implicit messages: Messages): PresentationNotificationAnswersViewModel = {
      val mode = CheckMode

      val helper = new PresentationNotificationAnswersHelper(userAnswers, departureId, mode)

      val firstSection = Section(
        rows = Seq(
          helper.limitDate,
          helper.containerIndicator
        ).flatten
      )

      val borderSection = Section(
        rows = Seq(
          helper.borderModeOfTransportYesNo,
          helper.borderModeOfTransport
        ).flatten
      )

      val placeOfLoading = Section(
        sectionTitle = messages("checkYourAnswers.placeOfLoading"),
        rows = Seq(
          helper.addUnlocodeYesNo,
          helper.unlocode,
          helper.addExtraInformationYesNo,
          helper.country,
          helper.location
        ).flatten
      )

      val addBorderMeansActiveSection = Section(
        rows = Seq(
          helper.addBorderMeansOfTransportYesNo
        ).flatten
      )

      val activeBorderTransportMeansSection: Seq[Section] =
        userAnswers
          .get(BorderActiveListSection)
          .getOrElse(
            userAnswers.departureData.Consignment.ActiveBorderTransportMeans match {
              case Some(departureActiveBorderMeans) => Json.toJson(departureActiveBorderMeans).as[JsArray]
              case None                             => JsArray()
            }
          )
          .value
          .zipWithIndex
          .flatMap {
            case (_, i) => activeBorderAnswersViewModelProvider.apply(userAnswers, departureId, mode, Index(i)).sections
          }
          .toSeq

      val sections = firstSection.toSeq ++ borderSection.toSeq ++ placeOfLoading.toSeq ++ addBorderMeansActiveSection.toSeq ++ activeBorderTransportMeansSection

      new PresentationNotificationAnswersViewModel(sections)
    }
    // scalastyle:on method.length
  }
}
