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
import models.{CheckMode, Index, NormalMode}
import navigation.SealGroupNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.ItemPage
import pages.transport.equipment.index.*
import pages.transport.equipment.index.seals.SealIdentificationNumberPage

class SealGroupNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new SealGroupNavigator(sealIndex)

  "SealGroupNavigator" - {
    "in Normal mode" - {
      val mode = NormalMode

      "must go from add another seal page" - {
        "to seal identification number page when user answers yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherSealPage(equipmentIndex), true)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), userAnswers, departureId, mode)
            .mustEqual(SealIdentificationNumberPage(equipmentIndex, sealIndex).route(userAnswers, departureId, mode).value)
        }

        "to tech difficulties when AddAnotherSealPage does not exist" in {
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }

        "to to goods reference item page when user answers no" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherSealPage(equipmentIndex), false)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), userAnswers, departureId, mode)
            .mustEqual(ItemPage(equipmentIndex, Index(0)).route(userAnswers, departureId, mode).value)
        }
      }
    }

    "in Check mode" - {
      val mode = CheckMode

      "must go from add another seal page" - {
        "to seal identification number page when user answers yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherSealPage(equipmentIndex), true)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), userAnswers, departureId, mode)
            .mustEqual(SealIdentificationNumberPage(equipmentIndex, sealIndex).route(userAnswers, departureId, mode).value)
        }

        "to to the cya page when user answers no and the items have been answered" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherSealPage(equipmentIndex), false)
            .setValue(ItemPage(equipmentIndex, itemIndex), arbitraryItem.arbitrary.sample.value)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "to to the item page when user answers no and the items have not been answered" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherSealPage(equipmentIndex), false)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex), userAnswers, departureId, mode)
            .mustEqual(ItemPage(equipmentIndex, Index(0)).route(userAnswers, departureId, mode).value)
        }
      }
    }
  }
}
