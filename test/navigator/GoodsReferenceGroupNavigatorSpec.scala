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
import models.{CheckMode, Index, NormalMode, UserAnswers}
import navigation.GoodsReferenceGroupNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.ItemPage
import pages.transport.equipment.index.*

class GoodsReferenceGroupNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator = new GoodsReferenceGroupNavigator(itemIndex)

  "GoodsReferenceGroupNavigator" - {
    "in Normal mode" - {
      val mode = NormalMode

      "must go from ApplyAnotherItemPage" - {
        "to Item page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex), true)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustEqual(ItemPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to AddAnotherEquipment page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex), false)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustEqual(controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode))
          }
        }
      }
    }

    "in Check mode" - {
      val mode = CheckMode

      "must go from ApplyAnotherItemPage" - {
        "to Item page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex), true)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustEqual(ItemPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to CYA page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex), false)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "to tech difficulties when ApplyAnotherItemPage does not exist" in {
          navigator
            .nextPage(ApplyAnotherItemPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }
      }
    }
  }
}
