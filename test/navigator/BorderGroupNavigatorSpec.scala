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

package navigator

import base.SpecBase
import generators.Generators
import models.*
import navigation.BorderGroupNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.AddAnotherBorderMeansOfTransportYesNoPage

class BorderGroupNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new BorderGroupNavigator(activeIndex)

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode

      "must go from AddAnotherBorderMeansOfTransportYesNoPage" - {
        "to IdentificationController when yes" in {
          val userAnswers = emptyUserAnswers.setValue(AddAnotherBorderMeansOfTransportYesNoPage, true)
          navigator
            .nextPage(AddAnotherBorderMeansOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, activeIndex))
        }

        "to CheckYourAnswersController when no" in {
          val userAnswers = emptyUserAnswers.setValue(AddAnotherBorderMeansOfTransportYesNoPage, false)
          navigator
            .nextPage(AddAnotherBorderMeansOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to tech difficulties when AddAnotherBorderMeansOfTransportYesNoPage does not exist" in {
          navigator
            .nextPage(AddAnotherBorderMeansOfTransportYesNoPage, emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }
      }
    }

    "in CheckMode" - {
      val mode = CheckMode

      "must go from AddAnotherBorderMeansOfTransportYesNoPage" - {
        "to CYA when no" in {
          val userAnswers = emptyUserAnswers.setValue(AddAnotherBorderMeansOfTransportYesNoPage, false)
          navigator
            .nextPage(AddAnotherBorderMeansOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }
    }
  }
}
