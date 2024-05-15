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
import config.Constants.AuthorisationTypeDeparture._
import generated._
import generators.Generators
import models.{CheckMode, Index, NormalMode, UserAnswers}
import navigation.EquipmentNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.index._
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.{AddAnotherTransportEquipmentPage, AddTransportEquipmentYesNoPage, ItemPage}

class EquipmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val navigator = new EquipmentNavigator

  "EquipmentNavigator" - {
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
            .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"), AuthorisationType03("2", SSE, "1235"))))
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(
              controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0))
            )

        }

        "to AddSealsYesNo page when user selects No and AuthType is not in CL253" in {
          val userAnswers = emptyUserAnswers
            .copy(departureData = basicIe015.copy(Authorisation = Nil))
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex))

        }

        "to session expired when AddContainerIdentificationNumberYesNoPage does not exist" in {
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.SessionExpiredController.onPageLoad())
        }
      }

      "Must go from AddAnotherTransportEquipmentPage" - {
        "when answered no must go to Check your answers page" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), false)
          navigator
            .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "when answered yes" - {
          "when ContainerIndicatorPage is true" - {
            "must navigate to AddContainerIdentificationNumberYesNoPage " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "test"))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, NormalMode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }

          "when ContainerIndicatorPage is false" - {

            "must navigate to SealIdentificationNumberPage when Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "test"), AuthorisationType03("2", ACR, "test2"))))
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
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "test2"))))
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
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "test2"))))
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

      "must go from ItemPage to Apply another Item page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .setValue(ItemPage(equipmentIndex, itemIndex), arbitraryItem.arbitrary.sample.value)

            navigator
              .nextPage(ItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
              .mustBe(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
        }
      }

      "must go from ApplyAnotherItempage" - {
        "to Item page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), true)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
                .mustBe(ItemPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to AddAnotherEquipment page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), false)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
                .mustBe(controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode))
          }
        }
      }

      "must go from container Identification number page" - {
        "to seals identification number page when declaration is simplified and Auth type is C523" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"), AuthorisationType03("1", SSE, "1235"))))

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
                  answers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "1235"))))

                navigator
                  .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
                  .mustBe(AddSealYesNoPage(equipmentIndex).route(updatedAnswers, departureId, mode).value)
            }
          }

          "when Auth type is not C523" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers.copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"))))
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
                    basicIe015
                      .copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"), AuthorisationType03("1", SSE, "1235")))
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
                    .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "1235"))))

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
                    .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"))))
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

        "to session expired when AddSealYesNoPage does not exist" in {
          navigator
            .nextPage(AddSealYesNoPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.SessionExpiredController.onPageLoad())
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
        "to session expired when AddAnotherSealPage does not exist" in {
          navigator
            .nextPage(AddAnotherSealPage(equipmentIndex, itemIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.SessionExpiredController.onPageLoad())
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
    "in Check mode" - {
      val mode = CheckMode

      "Must go from AddAnotherTransportEquipmentPage" - {
        "when answered no must go to Check your answers page" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), false)
          navigator
            .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

        "when answered yes" - {
          "when ContainerIndicatorPage is true" - {
            "must navigate to AddContainerIdentificationNumberYesNoPage " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "test"))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, true)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddContainerIdentificationNumberYesNoController
                    .onPageLoad(departureId, mode, equipmentIndex)
                )
            }
          }

          "when ContainerIndicatorPage is false" - {

            "must navigate to SealIdentificationNumberPage when Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "test"), AuthorisationType03("1", ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                )
            }

            "must navigate to AddSealYesNoPage when Not Simplified and the authorisation type = C523 " in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

            "must navigate to AddSealYesNoPage when Simplified and the authorisation type is not C523" in {
              val userAnswers = emptyUserAnswers
                .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "test2"))))
                .setValue(AddAnotherTransportEquipmentPage(equipmentIndex), true)
                .setValue(ContainerIndicatorPage, false)
              navigator
                .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), userAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex)
                )
            }

          }
        }
        "to session expired when AddAnotherTransportEquipmentPage does not exist" in {
          navigator
            .nextPage(AddAnotherTransportEquipmentPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.SessionExpiredController.onPageLoad())
        }
      }

      "must go from ItemPage to Apply another Item page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .setValue(ItemPage(equipmentIndex, itemIndex), arbitraryItem.arbitrary.sample.value)

            navigator
              .nextPage(ItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
              .mustBe(controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex))
        }
      }

      "must go from addTransportEquipmentPage" - {
        "to CYA page when answer is No" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddTransportEquipmentYesNoPage, false)

              navigator
                .nextPage(AddTransportEquipmentYesNoPage, updatedAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "to add seals page" - {
          "when declaration type is not simplified" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers
                    .setValue(AddTransportEquipmentYesNoPage, true)
                    .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", SSE, "1235"))))

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
                    .copy(departureData = basicIe015.copy(Authorisation = Seq(AuthorisationType03("1", ACR, "1234"))))
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

        "to the cya page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddSealYesNoPage(equipmentIndex), false)

              navigator
                .nextPage(AddSealYesNoPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }
      "must go from AddContainerIdentificationNumberYesNoPage" - {
        "to ContainerIdentificationNumber page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)

              navigator
                .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(
                  controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, NormalMode, equipmentIndex)
                )
          }
        }

        "to the cya page when user answers no and seal yesNo is answered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)
                  .setValue(AddSealYesNoPage(equipmentIndex), true)

              navigator
                .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), updatedAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "to the add seal yes no page when user answers no and addSealYesNo is not answered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), false)

              navigator
                .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), updatedAnswers, departureId, CheckMode)
                .mustBe(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, NormalMode, equipmentIndex))
          }
        }
        "to cya when AddContainerIdentificationNumberYesNoPage does not exist" in {
          navigator
            .nextPage(AddContainerIdentificationNumberYesNoPage(equipmentIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }

      }

      "must go from the container identification number page to CYA page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .setValue(ContainerIdentificationNumberPage(equipmentIndex), "67YU988")
                .setValue(AddSealYesNoPage(equipmentIndex), true)

            navigator
              .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }

      "must go from the container identification number page to AddSealYesNo when addSealYesNo is not defined" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .setValue(ContainerIdentificationNumberPage(equipmentIndex), "67YU988")

            navigator
              .nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode)
              .mustBe(controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, NormalMode, equipmentIndex))
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

      "to to the cya page when user answers no and the items have been answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddAnotherSealPage(equipmentIndex, sealIndex), false)
          .setValue(ItemPage(equipmentIndex, itemIndex), arbitraryItem.arbitrary.sample.value)
        navigator
          .nextPage(AddAnotherSealPage(equipmentIndex, sealIndex), userAnswers, departureId, mode)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
      }

      "to to the item page when user answers no and the items have not been answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(AddAnotherSealPage(equipmentIndex, sealIndex), false)
        navigator
          .nextPage(AddAnotherSealPage(equipmentIndex, sealIndex), userAnswers, departureId, mode)
          .mustBe(ItemPage(equipmentIndex, Index(0)).route(userAnswers, departureId, mode).value)
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

      "must go from ApplyAnotherItempage" - {

        "to Item page when user answers yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), true)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
                .mustBe(ItemPage(equipmentIndex, Index(0)).route(updatedAnswers, departureId, mode).value)
          }
        }

        "to CYA page when user answers no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .setValue(ApplyAnotherItemPage(equipmentIndex, itemIndex), false)

              navigator
                .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), updatedAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
        "to session expired when ApplyAnotherItemPage does not exist" in {
          navigator
            .nextPage(ApplyAnotherItemPage(equipmentIndex, itemIndex), emptyUserAnswers, departureId, mode)
            .mustBe(controllers.routes.SessionExpiredController.onPageLoad())
        }
      }

    }
  }
}
