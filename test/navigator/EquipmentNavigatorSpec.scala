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
import models.messages.{Authorisation, AuthorisationType}
import models.messages.AuthorisationType.C521
import models.{Index, NormalMode, UserAnswers}
import navigation.EquipmentNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.{AddSealYesNoPage, ContainerIdentificationNumberPage}
import pages.transport.equipment.index.seals.SealIdentificationNumberPage

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
                  TestMessageData.messageData.copy(Authorisation =
                    Some(Seq(Authorisation(C521, "1234"), Authorisation(AuthorisationType.Other("C523"), "1235")))
                  )
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
                  answers.copy(departureData =
                    TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(AuthorisationType.Other("C523"), "1235"))))
                  )

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
                      .copy(Authorisation = Some(Seq(Authorisation(C521, "1234"), Authorisation(AuthorisationType.Other("C523"), "1235"))))
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
                    .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(AuthorisationType.Other("C523"), "1235")))))

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

        "to goods reference item page when user answers no" ignore {
          // Todo Update when CTCP-3956 is completed
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
    }
  }
}
