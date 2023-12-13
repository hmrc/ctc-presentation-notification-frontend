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

package viewModels.transport.equipment

import base.SpecBase
import controllers.transport.equipment.index.seals.routes
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.equipment.index.AddSealYesNoPage
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import viewModels.ListItem
import viewModels.transport.equipment.AddAnotherSealViewModel.AddAnotherSealViewModelProvider

class AddAnotherSealViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one seal" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(SealIdentificationNumberPage(Index(0), Index(0)), identificationNumber)

          val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

          result.listItems.length mustBe 1
          result.title mustBe "You have added 1 seal"
          result.heading mustBe "You have added 1 seal"
          result.legend mustBe "Do you want to add another seal?"
          result.maxLimitLabel mustBe "You cannot add any more seals. To add another, you need to remove one first."
      }
    }

    "when there are multiple seals" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], nonEmptyString, Gen.choose(2, frontendAppConfig.maxSeals)) {
        (mode, identificationNumber, seals) =>
          val userAnswers = (0 until seals).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              acc
                .setValue(SealIdentificationNumberPage(equipmentIndex, Index(i)), s"$identificationNumber$i")
          }

          val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)
          result.listItems.length mustBe seals
          result.title mustBe s"You have added ${formatter.format(seals)} seals"
          result.heading mustBe s"You have added ${formatter.format(seals)} seals"
          result.legend mustBe "Do you want to add another seal?"
          result.maxLimitLabel mustBe "You cannot add any more seals. To add another, you need to remove one first."
      }
    }

    "when section is mandatory" - {
      "and one seal" in {
        forAll(arbitrary[Mode], nonEmptyString) {
          (mode, identificationNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), identificationNumber)

            val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

            result.listItems mustBe Seq(
              ListItem(
                name = identificationNumber,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
                removeUrl = None
              )
            )
        }
      }

      "and multiple seals" in {
        forAll(arbitrary[Mode], nonEmptyString, nonEmptyString) {
          (mode, identificationNumber1, identificationNumber2) =>
            val userAnswers = emptyUserAnswers
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), identificationNumber1)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(1)), identificationNumber2)

            val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

            result.listItems mustBe Seq(
              ListItem(
                name = identificationNumber1,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
                removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url)
              ),
              ListItem(
                name = identificationNumber2,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url,
                removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url)
              )
            )
        }
      }
    }

    "when section is optional" - {
      "and one seal" in {
        forAll(arbitrary[Mode], nonEmptyString) {
          (mode, identificationNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(AddSealYesNoPage(equipmentIndex), true)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), identificationNumber)

            val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

            result.listItems mustBe Seq(
              ListItem(
                name = identificationNumber,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
                removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url)
              )
            )
        }
      }

      "and multiple seals" in {
        forAll(arbitrary[Mode], nonEmptyString, nonEmptyString) {
          (mode, identificationNumber1, identificationNumber2) =>
            val userAnswers = emptyUserAnswers
              .setValue(AddSealYesNoPage(equipmentIndex), true)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), identificationNumber1)
              .setValue(SealIdentificationNumberPage(equipmentIndex, Index(1)), identificationNumber2)

            val result = new AddAnotherSealViewModelProvider().apply(userAnswers, departureId, mode, equipmentIndex)

            result.listItems mustBe Seq(
              ListItem(
                name = identificationNumber1,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url,
                removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, Index(0)).url)
              ),
              ListItem(
                name = identificationNumber2,
                changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url,
                removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, Index(1)).url)
              )
            )
        }
      }
    }
  }
}
