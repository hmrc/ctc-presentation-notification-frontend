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
import config.Constants.AuthorisationTypeDeparture.*
import generated.*
import generators.Generators
import models.{CheckMode, Index, NormalMode}
import navigation.EquipmentGroupNavigator
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.AddAnotherTransportEquipmentPage

class EquipmentGroupNavigatorSpec extends SpecBase with Generators {

  private val navigator = new EquipmentGroupNavigator(equipmentIndex)

  "EquipmentGroupNavigator" - {
    "in Normal mode" - {
      val mode = NormalMode

      "Must go from AddAnotherTransportEquipmentPage" - {
        "when answered no must go to Check your answers page" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportEquipmentPage, false)
          navigator
            .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, NormalMode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "when answered yes" - {
          "when ContainerIndicatorPage is true" - {
            "must navigate to AddContainerIdentificationNumberYesNoPage " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, ACR, "test"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, NormalMode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }

          "when ContainerIndicatorPage is false" - {

            "must navigate to SealIdentificationNumberPage when Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, SSE, "test"), AuthorisationType02(2, ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, NormalMode)
                .mustEqual(
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                )
            }

            "must navigate to AddSealYesNoPage when Not Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, SSE, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, NormalMode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

            "must navigate to AddSealYesNoPage when Simplified and the authorisation type is not C523" in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, NormalMode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }
        }
      }
    }

    "in Check mode" - {
      val mode = CheckMode

      "Must go from AddAnotherTransportEquipmentPage" - {
        "when answered no must go to Check your answers page" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportEquipmentPage, false)
          navigator
            .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "when answered yes" - {
          "when ContainerIndicatorPage is true" - {
            "must navigate to AddContainerIdentificationNumberYesNoPage " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, ACR, "test"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }

          "when ContainerIndicatorPage is false" - {

            "must navigate to SealIdentificationNumberPage when Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, SSE, "test"), AuthorisationType02(1, ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                )
            }

            "must navigate to AddSealYesNoPage when Not Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, SSE, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

            "must navigate to AddSealYesNoPage when Simplified and the authorisation type is not C523" in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType02(1, ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage, true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage, userAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }
        }

        "to tech difficulties when AddAnotherTransportEquipmentPage does not exist" in {
          navigator
            .nextPage(AddAnotherTransportEquipmentPage, emptyUserAnswers, departureId, mode)
            .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
        }
      }
    }
  }
}
