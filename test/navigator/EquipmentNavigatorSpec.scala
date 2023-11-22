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

import base.{SpecBase, TestMessageData}
import generators.Generators
import models.messages.Authorisation
import models.messages.AuthorisationType.{C521, C523}
import models.{Index, NormalMode, UserAnswers}
import navigation.EquipmentNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.index.{AddAnotherSealPage, AddSealYesNoPage, ContainerIdentificationNumberPage}
import pages.transport.equipment.{AddTransportEquipmentYesNoPage, ItemPage}

class EquipmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator = new EquipmentNavigator

  "EquipmentNavigator" - {
    "in Normal mode" - {
      val mode = NormalMode

      "must go from container Identification number page" - {
        "to seals identification number page when declaration is simplified and Auth type is C523" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers.copy(departureData =
                  TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "1234"), Authorisation(C523, "1235"))))
                )

              navigator
                .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(SealIdentificationNumberPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to add seals page" - {
          "when declaration type is not simplified" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers.copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C523, "1235")))))

                navigator
                  .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
                  .mustBe(AddSealYesNoPage(equipmentIndex).route(updatedAnswers, departureId, mode).value)
            }
          }

          "when Auth type is not C523" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers.copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "1234")))))
                navigator
                  .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
                  .mustBe(AddSealYesNoPage(equipmentIndex).route(updatedAnswers, departureId, mode).value)
            }
          }
        }
      }

      "must go from add transport equipment yes no page when user answers yes" - {
        "to seals identification number page when declaration is simplified and Auth type is C523" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddTransportEquipmentYesNoPage, true)
                  .copy(departureData =
                    TestMessageData.messageData
                      .copy(Authorisation = Some(Seq(Authorisation(C521, "1234"), Authorisation(C523, "1235"))))
                  )

              navigator
                .nextPage(AddTransportEquipmentYesNoPage, updatedAnswers, departureId, mode)
                .mustBe(SealIdentificationNumberPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to add seals page" - {
          "when declaration type is not simplified" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers
                    .setValue(AddTransportEquipmentYesNoPage, true)
                    .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C523, "1235")))))

                navigator
                  .nextPage(AddTransportEquipmentYesNoPage, updatedAnswers, departureId, mode)
                  .mustBe(AddSealYesNoPage(equipmentIndex).route(updatedAnswers, departureId, mode).value)
            }
          }

          "when Auth type is not C523" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers
                    .setValue(AddTransportEquipmentYesNoPage, true)
                    .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "1234")))))
                navigator
                  .nextPage(AddTransportEquipmentYesNoPage, updatedAnswers, departureId, mode)
                  .mustBe(AddSealYesNoPage(equipmentIndex).route(updatedAnswers, departureId, mode).value)
            }
          }
        }
      }

      "must go from add seal yes no page" - {
        "to seal identification number page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddSealYesNoPage(equipmentIndex), true)

              navigator
                .nextPage(AddSealYesNoPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(SealIdentificationNumberPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to goods reference item page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddSealYesNoPage(equipmentIndex), false)

              navigator
                .nextPage(AddSealYesNoPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(ItemPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }
      }

      "must go from the seal identification number page to add another seal page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "67YU988")

            navigator
              .nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), updatedAnswers, departureId, mode)
              .mustBe(controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex))
        }
      }

      "must go from add another seal page" - {
        "to seal identification number page when user answers yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "Seal1")
            .setValue(SealIdentificationNumberPage(equipmentIndex, Index(1)), "Seal2")
            .setValue(AddAnotherSealPage(equipmentIndex, Index(2)), true)
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex, Index(2)), userAnswers, departureId, mode)
            .mustBe(SealIdentificationNumberPage(equipmentIndex, Index(2)).route(userAnswers, departureId, mode).value)
        }
      }

      "to to goods reference item page when user answers no" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddAnotherSealPage(equipmentIndex, sealIndex), false)
        navigator
          .nextPage(AddAnotherSealPage(equipmentIndex, sealIndex), userAnswers, departureId, mode)
          .mustBe(ItemPage(equipmentIndex, Index(0)).route(userAnswers, departureId, mode).value)
      }
    }
  }
}
