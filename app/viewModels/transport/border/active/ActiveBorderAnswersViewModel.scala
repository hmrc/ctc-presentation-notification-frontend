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

import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.ActiveBorderTransportMeansAnswersHelper
import viewModels.Section

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class ActiveBorderAnswersViewModel(section: Section)

object ActiveBorderAnswersViewModel {

  class ActiveBorderAnswersViewModelProvider @Inject() (implicit executionContext: ExecutionContext) {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, index: Index)(implicit
      messages: Messages,
      hc: HeaderCarrier
    ): ActiveBorderAnswersViewModel = {
      val helper = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, mode, index)
      new ActiveBorderAnswersViewModel(helper.getSection())
    }
  }
}
