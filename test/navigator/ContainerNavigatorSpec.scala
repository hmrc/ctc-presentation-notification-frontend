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

import base.TestMessageData._
import base.{SpecBase, TestMessageData}
import config.Constants.NoSecurityDetails
import generators.Generators
import models.messages.MessageData
import models.{NormalMode, UserAnswers}
import navigation.ContainerNavigator
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.ContainerIndicatorPage
import pages.transport.border.BorderModeOfTransportPage

class ContainerNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new ContainerNavigator

  "ContainerNavigator" - {

    "must go from ContainerIndicatorPage" - {

      "to BorderModeOfTransportPage when security is between 1-3" in {
        val securityGen = Arbitrary.arbitrary[String](arbitrarySecurityCode)
        forAll(securityGen) {
          security =>
            val messageData: MessageData =
              MessageData(
                customsOfficeOfDeparture,
                customsOfficeOfDestination,
                transitOperation.copy(security = security),
                Some(authorisation),
                holderOfTheTransitProcedure,
                None,
                None,
                consignment
              )
            val userAnswers = emptyUserAnswers.copy(departureData = messageData)
            navigator
              .nextPage(ContainerIndicatorPage, userAnswers, departureId, NormalMode)
              .mustBe(BorderModeOfTransportPage.route(userAnswers, departureId, NormalMode).value)
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
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                .mustBe(
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
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                .mustBe(
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
                  TestMessageData.messageData.copy(
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(ContainerIndicatorPage, updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
    }

  }

}
