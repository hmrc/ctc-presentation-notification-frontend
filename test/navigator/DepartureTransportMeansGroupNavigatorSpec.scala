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
import navigation.DepartureTransportMeansGroupNavigator
import pages.transport.departureTransportMeans.*

class DepartureTransportMeansGroupNavigatorSpec extends SpecBase with Generators {

  private val navigator = new DepartureTransportMeansGroupNavigator(transportIndex)

  "DepartureTransportMeansGroupNavigator" - {

    "in NormalMode" - {
      val mode = NormalMode

      "must go from add another departure transport means page" - {
        "to TransportMeansIdentificationPage when user answers yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportMeansPage, true)
          navigator
            .nextPage(AddAnotherTransportMeansPage, userAnswers, departureId, mode)
            .mustEqual(
              controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, transportIndex)
            )
        }

        "to CYA page when user answers no" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportMeansPage, false)
          navigator
            .nextPage(AddAnotherTransportMeansPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to tech difficulties when AddAnotherTransportMeansPage does not exist" in {
          navigator
            .nextPage(AddAnotherTransportMeansPage, emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }
      }
    }

    "in CheckMode" - {
      val mode = CheckMode

      "must go from add another departure transport means page" - {
        "to TransportMeansIdentificationPage when user answers yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportMeansPage, true)
          navigator
            .nextPage(AddAnotherTransportMeansPage, userAnswers, departureId, mode)
            .mustEqual(
              controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, transportIndex)
            )
        }

        "to CYA page when user answers no" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportMeansPage, false)
          navigator
            .nextPage(AddAnotherTransportMeansPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to tech difficulties when AddAnotherTransportMeansPage does not exist" in {
          navigator
            .nextPage(AddAnotherTransportMeansPage, emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }
      }
    }
  }
}
