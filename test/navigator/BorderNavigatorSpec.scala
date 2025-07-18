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
import config.Constants.DeclarationTypeSecurity.{EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import controllers.transport.border.active.routes
import generated.*
import generators.Generators
import models.*
import models.reference.Nationality
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.transport.border.active.Identification
import navigation.BorderNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.active.*
import pages.transport.border.{AddBorderMeansOfTransportYesNoPage, AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import pages.transport.{AddInlandModeOfTransportYesNoPage, ContainerIndicatorPage, InlandModePage}

class BorderNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new BorderNavigator

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode

      "must go from Border mode of transport page" - {

        "when security is in set 1,2,3 to Identification page" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, true)
            .copy(departureData =
              basicIe015.copy(
                TransitOperation = basicIe015.TransitOperation.copy(security = EntrySummaryDeclarationSecurityDetails)
              )
            )
          navigator
            .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
            .mustEqual(
              controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, NormalMode, equipmentIndex)
            )
        }

        "when security is NoSecurityDetails and active border transport is present " - {
          "and container indicator equals true navigate to ContainerIdentificationNumber page " in {

            forAll(arbitrary[ActiveBorderTransportMeansType03]) {
              activeBorderTransportMeans =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, true)
                  .copy(departureData =
                    basicIe015.copy(
                      Consignment = basicIe015.Consignment.copy(
                        ActiveBorderTransportMeans = Seq(activeBorderTransportMeans),
                        containerIndicator = None
                      ),
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustEqual(
                    controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                  )
            }
          }

          "and container indicator equals false navigate to AddTransportEquipmentYesNo page " in {

            forAll(arbitrary[ActiveBorderTransportMeansType03]) {
              activeBorderTransportMeans =>
                val userAnswers = emptyUserAnswers
                  .setValue(ContainerIndicatorPage, false)
                  .copy(departureData =
                    basicIe015.copy(
                      Consignment = basicIe015.Consignment.copy(
                        ActiveBorderTransportMeans = Seq(activeBorderTransportMeans),
                        containerIndicator = None
                      ),
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustEqual(
                    controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, NormalMode)
                  )
            }
          }

          "and container indicator is not captured in IE170 navigate to check your answers page " in {

            forAll(arbitrary[ActiveBorderTransportMeansType03]) {
              activeBorderTransportMeans =>
                val userAnswers = emptyUserAnswers
                  .copy(departureData =
                    basicIe015.copy(
                      Consignment = basicIe015.Consignment.copy(
                        ActiveBorderTransportMeans = Seq(activeBorderTransportMeans),
                        containerIndicator = None
                      ),
                      TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                    )
                  )
                navigator
                  .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                  .mustEqual(
                    controllers.routes.CheckYourAnswersController.onPageLoad(departureId)
                  )
            }
          }
        }

        "when security is NoSecurityDetails and active border transport is not present" - {
          "and containerIndicator is true navigate to ContainerIdentificationNumber page " in {

            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .copy(departureData =
                basicIe015.copy(
                  Consignment = basicIe015.Consignment.copy(
                    ActiveBorderTransportMeans = Nil,
                    containerIndicator = None
                  ),
                  TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                )
              )
            navigator
              .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
              .mustEqual(ContainerIdentificationNumberPage(equipmentIndex).route(userAnswers, departureId, mode).value)
          }

          "and containerIndicator is false navigate to AddTransportEquipmentYesNo page " in {

            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)
              .copy(departureData =
                basicIe015.copy(
                  Consignment = basicIe015.Consignment.copy(
                    ActiveBorderTransportMeans = Nil,
                    containerIndicator = None
                  ),
                  TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                )
              )
            navigator
              .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
              .mustEqual(AddTransportEquipmentYesNoPage.route(userAnswers, departureId, mode).value)
          }

          "and container indicator is not captured in IE170 navigate to check your answers page " in {

            val userAnswers = emptyUserAnswers
              .copy(departureData =
                basicIe015.copy(
                  Consignment = basicIe015.Consignment.copy(
                    ActiveBorderTransportMeans = Nil,
                    containerIndicator = None
                  ),
                  TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                )
              )
            navigator
              .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
              .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

      "must go from identification page to identification number page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(IdentificationPage(activeIndex), answers, departureId, NormalMode)
              .mustEqual(routes.IdentificationNumberController.onPageLoad(departureId, NormalMode, activeIndex))
        }
      }

      "must go from identification number page to nationality page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(IdentificationNumberPage(activeIndex), answers, departureId, NormalMode)
              .mustEqual(routes.NationalityController.onPageLoad(departureId, NormalMode, activeIndex))
        }
      }

      "must go from nationality page to customs offices page" in {
        val destinationOffice = arbitrary[CustomsOfficeOfDestinationDeclaredType01].sample.value
        val transitOffice     = arbitrary[CustomsOfficeOfTransitDeclaredType06].sample.value
        val exitOffice        = arbitrary[CustomsOfficeOfExitForTransitDeclaredType02].sample.value

        val updatedDepartureData = basicIe015.copy(
          CustomsOfficeOfDestinationDeclared = destinationOffice,
          CustomsOfficeOfTransitDeclared = Seq(transitOffice),
          CustomsOfficeOfExitForTransitDeclared = Seq(exitOffice)
        )

        val userAnswers = emptyUserAnswers
          .copy(departureData = updatedDepartureData)

        navigator
          .nextPage(NationalityPage(activeIndex), userAnswers, departureId, NormalMode)
          .mustEqual(routes.CustomsOfficeActiveBorderController.onPageLoad(departureId, NormalMode, activeIndex))
      }

      "must go from to customs offices page to conveyance number page when security is 1,2,3 and border mode of transport is 4 " in {

        forAll(arbitrary[String](arbitrarySecurityDetailsNonZeroType), nonEmptyString) {
          (securityType, borderModeDesc) =>
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderMode("4", borderModeDesc))
              .copy(departureData =
                basicIe015.copy(
                  TransitOperation = basicIe015.TransitOperation.copy(security = securityType)
                )
              )
            navigator
              .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
              .mustEqual(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))

        }

      }

      "must go from to customs offices page to conveyance number page when security is 0 and border mode of transport is not 4 " in {

        forAll(arbitraryOptionalNonAirBorderModeOfTransport.arbitrary) {
          borderModeOfTransport =>
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, borderModeOfTransport)
              .copy(departureData =
                basicIe015.copy(
                  TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                )
              )
            navigator
              .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
              .mustEqual(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))

        }

      }

      "must go from add conveyance page yes no to conveyance number page when selected yes" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
        navigator
          .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), userAnswers, departureId, NormalMode)
          .mustEqual(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, NormalMode, activeIndex))
      }
      "to tech difficulties when AddConveyanceReferenceYesNoPage does not exist" in {
        navigator
          .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), emptyUserAnswers, departureId, mode)
          .mustEqual(controllers.routes.ErrorController.technicalDifficulties())
      }

      "when selected no on add conveyance number yes no" - {

        "must go to add another active border when customs office of transit is present" in {

          forAll(arbitrary[UserAnswers], arbitrary[CustomsOfficeOfTransitDeclaredType06]) {
            (answers, customsOffices) =>
              val updatedAnswers = answers
                .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)
                .copy(departureData =
                  basicIe015.copy(
                    CustomsOfficeOfTransitDeclared = Seq(customsOffices)
                  )
                )
              navigator
                .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustEqual(routes.AddAnotherBorderMeansOfTransportYesNoController.onPageLoad(departureId, NormalMode))
          }
        }

        "must go to check your answers page when customs office of transit is not present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .copy(departureData =
                  basicIe015.copy(
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)
              navigator
                .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

        }

      }

      "when on conveyance number page" - {

        "must go to add another active border when customs office of transit is present" in {

          forAll(arbitrary[UserAnswers], arbitrary[CustomsOfficeOfTransitDeclaredType06]) {
            (answers, customsOffices) =>
              val updatedAnswers = answers
                .copy(departureData =
                  basicIe015.copy(
                    CustomsOfficeOfTransitDeclared = Seq(customsOffices)
                  )
                )
              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), updatedAnswers, departureId, NormalMode)
                .mustEqual(routes.AddAnotherBorderMeansOfTransportYesNoController.onPageLoad(departureId, NormalMode))
          }
        }
        "must go to check your answers page when customs office of transit is not present" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val userAnswers = answers
                .copy(departureData =
                  basicIe015.copy(
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), userAnswers, departureId, NormalMode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "must go to CYA when CustomsOfficeOfTransitDeclared not defined and ContainerIndicatorPage is set to true" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val userAnswers = answers
                .copy(departureData =
                  basicIe015.copy(
                    CustomsOfficeOfTransitDeclared = Nil,
                    Consignment = basicIe015.Consignment.copy(
                      containerIndicator = Some(Number1)
                    )
                  )
                )
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(ConveyanceReferenceNumberPage(activeIndex), userAnswers, departureId, NormalMode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

    }

    "in CheckMode" - {
      val mode = CheckMode

      "must go from AddBorderModeOfTransportYesNoPage" - {

        "to CYA page when No " in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, false)
          navigator
            .nextPage(AddBorderModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to BorderModeOfTransportPage when Yes and there is no answer to border mode of transport" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, true)
            .copy(departureData =
              emptyUserAnswers.departureData.copy(Consignment = emptyUserAnswers.departureData.Consignment.copy(modeOfTransportAtTheBorder = None))
            )
          navigator
            .nextPage(AddBorderModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode))

        }

        "to CheckYourAnswers when Yes and there is an answer to border mode of transport in ie170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, true)
            .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
          navigator
            .nextPage(AddBorderModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      }

      "must go from AddInlandModeYesNoPage" - {

        "to CYA page when No " in {

          val userAnswers = emptyUserAnswers
            .setValue(AddInlandModeOfTransportYesNoPage, false)
          navigator
            .nextPage(AddInlandModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to InlandModePage when Yes and there is no answer to border mode of transport" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddInlandModeOfTransportYesNoPage, true)
            .copy(departureData =
              emptyUserAnswers.departureData.copy(Consignment = emptyUserAnswers.departureData.Consignment.copy(inlandModeOfTransport = None))
            )
          navigator
            .nextPage(AddInlandModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode))

        }

        "to CheckYourAnswers when Yes and there is an answer to inlandMode in ie170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddInlandModeOfTransportYesNoPage, true)
            .setValue(InlandModePage, InlandMode("1", "Air"))
          navigator
            .nextPage(AddInlandModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      }

      "must go from InlandModePage" - {

        "to CYA page when InlandMode is 5" in {

          val userAnswers = emptyUserAnswers
            .setValue(InlandModePage, InlandMode("5", "test"))
          navigator
            .nextPage(InlandModePage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to DepartureMeansIdentificationPage when InlandMode is not 5" in {
          val userAnswers = emptyUserAnswers
            .setValue(InlandModePage, InlandMode("4", "test"))

          navigator
            .nextPage(InlandModePage, userAnswers, departureId, mode)
            .mustEqual(
              controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, mode, transportIndex)
            )
        }

        "to CheckYourAnswers when Yes and there is an answer to inlandMode in ie170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddInlandModeOfTransportYesNoPage, true)
            .setValue(InlandModePage, InlandMode("1", "Air"))
          navigator
            .nextPage(AddInlandModeOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      }

      "must go from BorderModeOfTransportPage to" - {
        "Add border means of transport page when Do you want to add border mode of transport?  is Yes and security type is 0" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, true)
            .copy(departureData =
              emptyUserAnswers.departureData.copy(TransitOperation = emptyUserAnswers.departureData.TransitOperation.copy(security = NoSecurityDetails))
            )
          navigator
            .nextPage(BorderModeOfTransportPage, userAnswers, departureId, CheckMode)
            .mustEqual(controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode))

        }
        "to identification page when Do you want to add border mode of transport?  is false and security type is 1" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, false)
            .copy(departureData = emptyUserAnswers.departureData.copy(TransitOperation = emptyUserAnswers.departureData.TransitOperation.copy(security = "1")))

          navigator
            .nextPage(BorderModeOfTransportPage, userAnswers, departureId, CheckMode)
            .mustEqual(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, CheckMode, Index(0)))

        }

        "border.routes.AddBorderMeansOfTransport page" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddBorderModeOfTransportYesNoPage, false)
            .copy(departureData = emptyUserAnswers.departureData.copy(TransitOperation = emptyUserAnswers.departureData.TransitOperation.copy(security = "1")))

          navigator
            .nextPage(BorderModeOfTransportPage, userAnswers, departureId, CheckMode)
          Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }

      "must go from Border mode of transport page" - {

        "to identification page when security mode of transport at border is not 5, security is 1,2,3 and active border transport is not present " in {

          forAll(arbitraryOptionalNonMailBorderModeOfTransport.arbitrary, arbitrary[String](arbitrarySecurityDetailsNonZeroType)) {
            (borderModeOfTransport, securityType) =>
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
                .copy(departureData =
                  basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = securityType)
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustEqual(IdentificationPage(activeIndex).route(userAnswers, departureId, mode).value)

          }

        }

        "to more add active border transport means page when security is 0 and active border transport is present " in {

          forAll(arbitrary[ActiveBorderTransportMeansType03]) {
            activeBorderTransportMeans =>
              val userAnswers = emptyUserAnswers
                .copy(departureData =
                  basicIe015.copy(
                    Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Seq(activeBorderTransportMeans)),
                    TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails)
                  )
                )
              navigator
                .nextPage(BorderModeOfTransportPage, userAnswers, departureId, mode)
                .mustEqual(controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode))
          }
        }
      }

      "must go from AddBorderMeansOfTransportYesNoPage" - {

        "to CYA page when No " in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderMeansOfTransportYesNoPage, false)
          navigator
            .nextPage(AddBorderMeansOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to Identification Page when Yes and the active border list section does not exist in ie170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderMeansOfTransportYesNoPage, true)
            .copy(departureData =
              emptyUserAnswers.departureData.copy(Consignment = emptyUserAnswers.departureData.Consignment.copy(ActiveBorderTransportMeans = Nil))
            )
          navigator
            .nextPage(AddBorderMeansOfTransportYesNoPage, userAnswers, departureId, mode)
            .mustEqual(controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, activeIndex))

        }
      }

      "must go from identification page" - {

        "to identificationNumberPage when identification does not exist in the ie70" in {
          navigator
            .nextPage(IdentificationPage(activeIndex), emptyUserAnswers, departureId, CheckMode)
            .mustEqual(routes.IdentificationNumberController.onPageLoad(departureId, CheckMode, activeIndex))

        }

        "to CYA page when identification number exist in the 170" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddBorderMeansOfTransportYesNoPage, true)
            .setValue(IdentificationPage(activeIndex), Identification("Air", "desc"))
            .setValue(IdentificationNumberPage(activeIndex), "identificationNumber")
          navigator
            .nextPage(IdentificationPage(activeIndex), userAnswers, departureId, CheckMode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      }

      "must go from identification number page" - {

        "to nationality page when identification number does not exist in the ie70" in {
          navigator
            .nextPage(IdentificationNumberPage(activeIndex), emptyUserAnswers, departureId, CheckMode)
            .mustEqual(routes.NationalityController.onPageLoad(departureId, CheckMode, activeIndex))

        }

        "to CYA page when identification number does exist in the 170 and nationality exists" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddBorderMeansOfTransportYesNoPage, true)
            .setValue(NationalityPage(activeIndex), Nationality("AR", "Argentina"))
          navigator
            .nextPage(IdentificationNumberPage(activeIndex), userAnswers, departureId, CheckMode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }
      }

      "must go from nationality page" - {

        "to customs office ref number page when customs office ref number does not exist in the ie170" in {
          navigator
            .nextPage(NationalityPage(activeIndex), emptyUserAnswers, departureId, CheckMode)
            .mustEqual(routes.CustomsOfficeActiveBorderController.onPageLoad(departureId, CheckMode, activeIndex))

        }

        "to CYA page when customs office ref number does exist in the 170" in {
          forAll(arbitraryCustomsOffice.arbitrary) {
            customsOffice =>
              val userAnswers = emptyUserAnswers
                .setValue(AddBorderMeansOfTransportYesNoPage, true)
                .setValue(CustomsOfficeActiveBorderPage(activeIndex), customsOffice)
              navigator
                .nextPage(NationalityPage(activeIndex), userAnswers, departureId, CheckMode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

      "from the customs offices page" - {

        "if security is 1,2,3 and border mode of transport is 4 " - {

          "go to the ConveyanceRefNumberPage if it is not in the ie170" in {
            forAll(arbitrary[String](arbitrarySecurityDetailsNonZeroType)) {
              securityType =>
                val userAnswers =
                  emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
                    .copy(departureData =
                      basicIe015.copy(
                        TransitOperation = basicIe015.TransitOperation.copy(security = securityType)
                      )
                    )
                navigator
                  .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
                  .mustEqual(routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))
            }
          }

          "go to CYA page if ConveyanceRefNumber is in the 170" in {
            forAll(arbitrary[String](arbitrarySecurityDetailsNonZeroType), nonEmptyString, nonEmptyString) {
              (securityType, borderModeDesc, conveyanceRefNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode("4", borderModeDesc))
                  .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)
                  .copy(departureData =
                    basicIe015.copy(
                      TransitOperation = basicIe015.TransitOperation.copy(security = securityType)
                    )
                  )
                navigator
                  .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
                  .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
            }
          }

        }

        "if security is 1,2,3 and border mode of transport is not 4 " - {

          "go to the AddConveyanceRefNumberYesNoPage if it is not in the ie170" in {
            navigator
              .nextPage(CustomsOfficeActiveBorderPage(activeIndex), emptyUserAnswers, departureId, mode)
              .mustEqual(routes.AddConveyanceReferenceYesNoController.onPageLoad(departureId, mode, activeIndex))
          }

          "go to CYA page if AddConveyanceRefNumberYesNo is in the 170" in {
            forAll(arbitrary[String](arbitrarySecurityDetailsNonZeroType), nonEmptyString, nonEmptyString) {
              (securityType, borderModeDesc, conveyanceRefNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode("3", borderModeDesc))
                  .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                  .copy(departureData =
                    basicIe015.copy(
                      TransitOperation = basicIe015.TransitOperation.copy(security = securityType)
                    )
                  )
                navigator
                  .nextPage(CustomsOfficeActiveBorderPage(activeIndex), userAnswers, departureId, mode)
                  .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
            }
          }

        }

      }

      "must go from AddConveyanceReferenceNumberYesNoPage" - {

        "to CYA page when No " in {

          val userAnswers = emptyUserAnswers
            .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)
          navigator
            .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), userAnswers, departureId, mode)
            .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to ConveyanceReferenceNumberPage when Yes and conveyanceRefNumber does not exist ie170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
            .copy(departureData =
              emptyUserAnswers.departureData.copy(Consignment = emptyUserAnswers.departureData.Consignment.copy(ActiveBorderTransportMeans = Nil))
            )
          navigator
            .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), userAnswers, departureId, mode)
            .mustEqual(controllers.transport.border.active.routes.ConveyanceReferenceNumberController.onPageLoad(departureId, mode, activeIndex))

        }

        "to CheckYourAnswers when Yes and there is an answer to conveyance ref number in ie170" in {

          forAll(nonEmptyString) {
            conveyanceRefNumber =>
              val userAnswers = emptyUserAnswers
                .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)
              navigator
                .nextPage(AddConveyanceReferenceYesNoPage(activeIndex), userAnswers, departureId, mode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

          }
        }
      }

      "must go from ConveyanceRefNumberPage to CheckYourAnswers page" in {
        navigator
          .nextPage(ConveyanceReferenceNumberPage(activeIndex), emptyUserAnswers, departureId, CheckMode)
          .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }

    }
  }

}
