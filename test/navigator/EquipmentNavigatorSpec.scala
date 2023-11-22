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
import models._
import models.messages.AuthorisationType.{C521, C523}
import models.messages.{Authorisation, AuthorisationType}
import navigation.EquipmentNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.AddAnotherTransportEquipmentPage
import pages.transport.equipment.index.AddContainerIdentificationNumberYesNoPage

class EquipmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new EquipmentNavigator

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode
      "Must go from AddContainerIdentifierNumberPage" - {
        "to ContainerIdentifierNumberPage when user selects Yes" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex))

        }

        "to SealIdentifierNumberPage when user selects No and AuthType is in CL253" in {
          val userAnswers = emptyUserAnswers
            .copy(departureData =
              TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "1234"), Authorisation(AuthorisationType.Other("C523"), "1235"))))
            )
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(
              controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0))
            )

        }

        "to AddSealsYesNo page when user selects No and AuthType is not in CL253" in {
          val userAnswers = emptyUserAnswers
            .copy(departureData = TestMessageData.messageData.copy(Authorisation = None))
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))

        }

      }

      "Must go from AddAnotherTransportEquipmentPage" - {
        "when answered no must go to Check your answers page" ignore { //todo action when CYA page built
                  val userAnswers = emptyUserAnswers
                    .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), false)
                  navigator
                    .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                    .mustBe (controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))//todo when CYA page built will go there
                }

        "when answered yes" - {
          "when ContainerIndicatorPage is true" - {
            "must navigate to AddContainerIdentificationNumberYesNoPage " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "test")))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex.next)
                )
            }
          }
          "when ContainerIndicatorPage is false" - {

            "must navigate to SealIdentificationNumberPage when Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C523, "test"), Authorisation(C521, "test2")))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                )
            }

            "must navigate to AddSealYesNoPage when Not Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C523, "test2")))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

            "must navigate to AddSealYesNoPage when Simplified and the authorisation type is not C523" in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = TestMessageData.messageData.copy(Authorisation = Some(Seq(Authorisation(C521, "test2")))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

          }
        }

      }

    }

  }

}
