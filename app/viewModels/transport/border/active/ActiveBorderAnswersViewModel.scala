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

import config.FrontendAppConfig
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import play.api.i18n.Messages
import utils.ActiveBorderTransportMeansAnswersHelper
import viewModels.Section

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class ActiveBorderAnswersViewModel(sections: Seq[Section])

object ActiveBorderAnswersViewModel {

  class ActiveBorderAnswersViewModelProvider @Inject() (implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext) {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, index: Index)(implicit messages: Messages): ActiveBorderAnswersViewModel = {
      val helper = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, mode, index)
      val lastIndex = Index(
        userAnswers
          .get(BorderActiveListSection)
          .map(_.value.length - 1)
          .getOrElse(userAnswers.departureData.Consignment.ActiveBorderTransportMeans.map(_.length - 1).getOrElse(0))
      )

      val activeBorderSection =
        Section(
          sectionTitle = messages("checkYourAnswers.transportMeans.active.withIndex", index.display),
          rows = Seq(
            if (userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isDefined) helper.addBorderMeansOfTransportYesNo else None,
            helper.identificationType,
            helper.identificationNumber,
            helper.nationality,
            helper.customsOffice,
            helper.conveyanceReferenceNumberYesNo,
            helper.conveyanceReferenceNumber
          ).flatten,
          addAnotherLink = (userAnswers.departureData.CustomsOfficeOfTransitDeclared, lastIndex == index) match {
            case (Some(_), true) => helper.addOrRemoveActiveBorderTransportsMeans()
            case _               => None
          }
        )

      val sections = Seq(activeBorderSection)
      helper.addOrRemoveActiveBorderTransportsMeans()
      new ActiveBorderAnswersViewModel(sections)

    }
  }
}
