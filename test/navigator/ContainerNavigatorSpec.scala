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
import config.Constants.DeclarationTypeSecurity.NoSecurityDetails
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.ContainerNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.ContainerIndicatorPage
import pages.transport.border.BorderModeOfTransportPage

class ContainerNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new ContainerNavigator

  "ContainerNavigator" - {

    "when in Normal Mode" - {
      "must go from ContainerIndicatorPage" - {

        "to BorderModeOfTransportPage when security is between 1-3" in {
          forAll(arbitrary[String](arbitrarySecurityDetailsNonZeroType)) {
            security =>
              val userAnswers = emptyUserAnswers.copy(departureData =
                basicIe015.copy(
                  TransitOperation = basicIe015.TransitOperation.copy(security = security)
                )
              )
              navigator
                .nextPage(ContainerIndicatorPage, userAnswers, departureId, NormalMode)
                .mustEqual(BorderModeOfTransportPage.route(userAnswers, departureId, NormalMode).value)
          }
        }

        "to ContainerIdentificationNumber page" +
          "when security is '0'" +
          "and containerIndicator has been answered as true in IE170" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    basicIe015.copy(
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails),
                      Consignment = basicIe015.Consignment.copy(containerIndicator = None)
                    )
                  )
                navigator
                  .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                  .mustEqual(
                    controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                  )
            }
          }

        "to AddTransportEquipmentYesNo page" +
          "when security is '0'" +
          "and containerIndicator has been answered as false in IE170" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .setValue(ContainerIndicatorPage, false)
                  .copy(departureData =
                    basicIe015.copy(
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails),
                      Consignment = basicIe015.Consignment.copy(containerIndicator = None)
                    )
                  )
                navigator
                  .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                  .mustEqual(
                    controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, NormalMode)
                  )
            }
          }

        "to CYA Page" +
          "when security is '0'" +
          "and containerIndicator has not been answered in IE170" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .copy(departureData =
                    basicIe015.copy(
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                  .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
            }
          }
      }
    }

    "when in Check Mode" - {

      "to Container indicator page when answer is Yes" in {

        val userAnswers = emptyUserAnswers.setValue(ContainerIndicatorPage, true)
        navigator
          .nextPage(ContainerIndicatorPage, userAnswers, departureId, CheckMode)
          .mustEqual(controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, CheckMode, equipmentIndex))
      }

      "to Add Transport Equipment Page when answer is No" in {

        val userAnswers = emptyUserAnswers.setValue(ContainerIndicatorPage, false)
        navigator
          .nextPage(ContainerIndicatorPage, userAnswers, departureId, CheckMode)
          .mustEqual(controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, CheckMode))
      }

      "to tech difficulties when ContainerIndicatorPage does not exist" in {
        navigator
          .nextPage(ContainerIndicatorPage, emptyUserAnswers, departureId, CheckMode)
          .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
      }

    }

  }

}
