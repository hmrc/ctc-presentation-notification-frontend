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

import base.TestMessageData.{consignment, customsOfficeOfTransitDeclared, transitOperation}
import base.{SpecBase, TestMessageData}
import config.Constants._
import controllers.transport.border.active.routes
import generators.Generators
import models._
import models.messages.{CustomsOfficeOfExitForTransitDeclared, CustomsOfficeOfTransitDeclared}
import models.reference.{BorderMode, CustomsOffice}
import navigation.BorderNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.MoreInformationPage
import pages.transport.ContainerIndicatorPage
import pages.transport.border.active._
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import pages.transport.border.{AddAnotherBorderModeOfTransportPage, BorderModeOfTransportPage}
import pages.transport.equipment.index.ContainerIdentificationNumberPage

class BorderNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new BorderNavigator

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode
      "must go from Border mode of transport page" - {

        "when security mode of transport at border is Mail, security is NoSecurityDetails and active border transport is present " - {
          "and container indicator equals true navigate to ContainerIdentificationNumber page " in {

            forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
              (activeBorderTransportMeans, borderModeDesc) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode(Mail, borderModeDesc))
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                      TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(
                    controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                  )
            }
          }

          "and container indicator equals false navigate to AddTransportEquipmentYesNo page " in {

            forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
              (activeBorderTransportMeans, borderModeDesc) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode(Mail, borderModeDesc))
                  .setValue(ContainerIndicatorPage, false)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                      TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(
                    controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, NormalMode)
                  )
            }
          }
        }

        "when security mode of transport at border is not Mail, security is not NoSecurityDetails and active border transport is not present " - {
          "and container indicator equals true must navigate to ContainerIdentificationNumber page " in {
            forAll(nonEmptyString) {
              borderModeDesc =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode(Rail, borderModeDesc))
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      TransitOperation = transitOperation.copy(security = EntrySummaryDeclarationSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(
                    controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                  )
            }
          }
          "and container indicator equals false must navigate to AddTransportEquipment page " in {
            forAll(nonEmptyString) {
              borderModeDesc =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode(Rail, borderModeDesc))
                  .setValue(ContainerIndicatorPage, false)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      TransitOperation = transitOperation.copy(security = EntrySummaryDeclarationSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(
                    controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode)
                  )
            }
          }

        }

        "to more information page when security mode of transport at border is not 5" +
          "and security is EntrySummaryDeclarationSecurityDetails,ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails" +
          "and active border transport is present" +
          "and containerIndicator is present " in {

            forAll(arbitraryOptionalNonMailBorderModeOfTransport.arbitrary, arbitrarySecurityDetailsNonZeroType.arbitrary) {
              (borderModeOfTransport, securityType) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, borderModeOfTransport)
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      TransitOperation = transitOperation.copy(security = securityType)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(ContainerIdentificationNumberPage(equipmentIndex).route(userAnswers, departureId, mode).value)
            }

          }

        "to container identification number page when security mode of transport at border is 5" +
          "and security is NoSecurityDetails" +
          "and active border transport is present" +
          "and containerIndicator is present" in {

            forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
              (activeBorderTransportMeans, borderModeDesc) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode("5", borderModeDesc))
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                      TransitOperation = transitOperation.copy(security = "0")
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(ContainerIdentificationNumberPage(equipmentIndex).route(userAnswers, departureId, mode).value)
            }
          }

        "to transport equipment page when security mode of transport at border is 5" +
          "and security is NoSecurityDetails" +
          "and active border transport is present" +
          "and containerIndicator not present" in {

            forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
              (activeBorderTransportMeans, borderModeDesc) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode("5", borderModeDesc))
                  .setValue(ContainerIndicatorPage, false)
                  .copy(departureData =
                    TestMessageData.messageData.copy(
                      Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                      TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustBe(AddTransportEquipmentYesNoPage.route(userAnswers, departureId, mode).value)
            }
          }
      }

      "when security mode of transport at border is not Mail, security is NoSecurityDetails and active border transport is present " - {
        "and container indicator equals true must navigate to ContainerIdentificationNumber page " in {
          forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
            (activeBorderTransportMeans, borderModeDesc) =>
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderMode(NoSecurityDetails, borderModeDesc))
                .setValue(ContainerIndicatorPage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                )
          }
        }
        "and container indicator equals false must navigate to AddTransportEquipment page " in {
          forAll(arbitraryActiveBorderTransportMeans.arbitrary, nonEmptyString) {
            (activeBorderTransportMeans, borderModeDesc) =>
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderMode(Rail, borderModeDesc))
                .setValue(ContainerIndicatorPage, false)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Consignment = consignment.copy(ActiveBorderTransportMeans = activeBorderTransportMeans),
                    TransitOperation = transitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode)
                )
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

      "must go from to customs offices page to conveyance number page when security is 1,2,3 and border mode of transport is 4 " in {

        forAll(arbitrarySecurityDetailsNonZeroType.arbitrary, nonEmptyString) {
          (securityType, borderModeDesc) =>
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderMode("4", borderModeDesc))
              .copy(departureData =
                TestMessageData.messageData.copy(
                  TransitOperation = transitOperation.copy(security = securityType)
                )
              )
            navigator
              .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
              .mustBe(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))

        }

      }

      "must go from to customs offices page to conveyance number page when security is 0 and border mode of transport is not 4 " in {

        forAll(arbitraryOptionalNonAirBorderModeOfTransport.arbitrary) {
          borderModeOfTransport =>
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, borderModeOfTransport)
              .copy(departureData =
                TestMessageData.messageData.copy(
                  TransitOperation = transitOperation.copy(security = "0")
                )
              )
            navigator
              .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
              .mustBe(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))

        }

      }

      "must go from add conveyance page yes no to conveyance number page when selected yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
        navigator
          .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), userAnswers, departureId, NormalMode)
          .mustBe(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, NormalMode, activeIndex))
      }

    }

    "when selected no on add conveyance number yes no" - {

      "must go to add another active border when customs office of transit is present" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)
              .copy(departureData =
                TestMessageData.messageData.copy(
                  CustomsOfficeOfTransitDeclared = customsOfficeOfTransitDeclared
                )
              )
            navigator
              .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), updatedAnswers, departureId, NormalMode)
              .mustBe(routes.AddAnotherBorderTransportController.onPageLoad(departureId, NormalMode))
        }
      }
      "must go to final border cya page when customs office of transit is not present" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)
            navigator
              .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), updatedAnswers, departureId, NormalMode)
              .mustBe(controllers.routes.MoreInformationController.onPageLoad(departureId)) // TODO: Update to be final border CYA page once implemented
        }

      }

    }

    "when on conveyance number page" - {

      "must go to add another active border when customs office of transit is present" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .copy(departureData =
                TestMessageData.messageData.copy(
                  CustomsOfficeOfTransitDeclared = customsOfficeOfTransitDeclared
                )
              )
            navigator
              .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
              .mustBe(routes.AddAnotherBorderTransportController.onPageLoad(departureId, NormalMode))
        }
      }

      "when customs office of transit is not present, container indicator is present in IE170" - {
        "must go to container identification number page when container indicator is true" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    CustomsOfficeOfTransitDeclared = None
                  )
                )
                .setValue(ContainerIndicatorPage, true)

              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                )
          }
        }

        "must go to container identification number page when container indicator is false" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    CustomsOfficeOfTransitDeclared = None
                  )
                )
                .setValue(ContainerIndicatorPage, false)

              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, NormalMode))
          }
        }
      }
      "must go to final border CYA page when customs office of transit is not present and container indicator is not present in IE170" in { // TODO: Update to be final border CYA page once implemented

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ConveyanceReferenceNumberPage(activeIndex), answers, departureId, NormalMode)
              .mustBe(controllers.routes.MoreInformationController.onPageLoad(departureId)) // TODO: Update to be final border CYA page once implemented
        }
      }
    }

    "when on add another border page" - {

      "must go to identification page when true" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(AddAnotherBorderModeOfTransportPage(activeIndex), true)

            navigator
              .nextPage(AddAnotherBorderModeOfTransportPage(activeIndex), updatedAnswers, departureId, NormalMode)
              .mustBe(routes.IdentificationController.onPageLoad(departureId, NormalMode, activeIndex.next))
        }
      }

      "when no" - {
        "and container indicator is true must go to container identification number page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    CustomsOfficeOfTransitDeclared = None
                  )
                )
                .setValue(ContainerIndicatorPage, true)

              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                )
          }
        }

        "and container indicator is false must go to container identification number page when" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    CustomsOfficeOfTransitDeclared = None
                  )
                )
                .setValue(ContainerIndicatorPage, false)

              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustBe(controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, NormalMode))
          }
        }
      }
      "must go to final border CYA page when customs office of transit is not present and container indicator is not present in IE170" in { // TODO: Update to be final border CYA page once implemented

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ConveyanceReferenceNumberPage(activeIndex), answers, departureId, NormalMode)
              .mustBe(controllers.routes.MoreInformationController.onPageLoad(departureId)) // TODO: Update to be final border CYA page once implemented
        }
      }
    }

  }
}
