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

import base.TestMessageData.{consignment, transitOperation}
import base.{SpecBase, TestMessageData}
import controllers.transport.border.active.routes
import generators.Generators
import models._
import models.messages.{CustomsOfficeOfExitForTransitDeclared, CustomsOfficeOfTransitDeclared}
import models.reference.{BorderMode, CustomsOffice}
import navigation.BorderNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.MoreInformationPage
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.border.active._

class BorderNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new BorderNavigator

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode
      "must go from Border mode of transport page" - {

        "to identification page when security mode of transport at border is not 5, security is 1,2,3 and active border transport is not present " in {

          forAll(arbitraryOptionalNonMailBorderModeOfTransport.arbitrary, arbitrarySecurityDetailsNonZeroType.arbitrary) {
            (borderModeOfTransport, securityType) =>
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Consignment = consignment.copy(ActiveBorderTransportMeans = None),
                    TransitOperation = transitOperation.copy(security = securityType)
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustBe(IdentificationPage(activeIndex).route(userAnswers, departureId, mode).value)

          }

        }

        //TODO: Change more information page to other page when created
        "to more information page when security mode of transport at border is  5, security is 0 and active border transport is  present " in {

          forAll(arbitraryActiveBorderTransportMeans.arbitrary) {
            activeBorderTransportMeans =>
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderMode("5", "Mail (Active mode of transport unknown)"))
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                    TransitOperation = transitOperation.copy(security = "0")
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustBe(MoreInformationPage.route(userAnswers, departureId, mode).value)
          }
        }
      }

      "must go from identification page to identification number page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(IdentificationPage(activeIndex), answers, departureId, NormalMode)
              .mustBe(routes.IdentificationNumberController.onPageLoad(departureId, NormalMode, activeIndex))
        }
      }

      "must go from identification number page to nationality page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(IdentificationNumberPage(activeIndex), answers, departureId, NormalMode)
              .mustBe(routes.NationalityController.onPageLoad(departureId, NormalMode, activeIndex))
        }
      }

      "must go from nationality page to customs offices page" in {
        val exitOffice        = arbitrary[CustomsOffice].sample.value
        val transitOffice     = arbitrary[CustomsOffice].sample.value
        val destinationOffice = arbitrary[CustomsOffice].sample.value

        val updatedDepartureData = emptyUserAnswers.departureData.copy(
          CustomsOfficeOfDestination = destinationOffice.id,
          CustomsOfficeOfTransitDeclared = Some(Seq(CustomsOfficeOfTransitDeclared(transitOffice.id))),
          CustomsOfficeOfExitForTransitDeclared = Some(Seq(CustomsOfficeOfExitForTransitDeclared(exitOffice.id)))
        )

        val userAnswers = emptyUserAnswers
          .copy(departureData = updatedDepartureData)

        navigator
          .nextPage(NationalityPage(activeIndex), userAnswers, departureId, NormalMode)
          .mustBe(routes.CustomsOfficeActiveBorderController.onPageLoad(departureId, NormalMode, activeIndex))
      }

    }

  }
}
