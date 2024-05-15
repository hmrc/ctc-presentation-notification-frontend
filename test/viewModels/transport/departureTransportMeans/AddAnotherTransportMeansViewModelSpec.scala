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

package viewModels.transport.departureTransportMeans

import base.SpecBase
import config.Constants.TransportModeCode.{Air, Mail}
import generators.Generators
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans._
import viewModels.ListItem
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel.AddAnotherTransportMeansViewModelProvider

class AddAnotherTransportMeansViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one departure transport means" in {
      forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], nonEmptyString) {
        (mode, identification, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(Index(0)), identification)
            .setValue(TransportMeansIdentificationNumberPage(Index(0)), identificationNumber)

          val result = new AddAnotherTransportMeansViewModelProvider()(userAnswers, departureId, mode)
          result.listItems.length mustBe 1
          result.title mustBe "You have added 1 departure means of transport"
          result.heading mustBe "You have added 1 departure means of transport"
          result.legend mustBe "Do you want to add another departure means of transport?"
      }
    }

    "when there are 3 or more departure transport means" - {

      "and inland mode is Road" in {
        val formatter = java.text.NumberFormat.getIntegerInstance

        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], nonEmptyString, Gen.choose(2, frontendAppConfig.maxTransportMeans)) {
          (mode, identification, identificationNumber, departureTransportMeans) =>
            val userAnswersInlandMode = emptyUserAnswers.setValue(InlandModePage, InlandMode("3", "Road"))
            val userAnswers = (0 until departureTransportMeans).foldLeft(userAnswersInlandMode) {
              (acc, i) =>
                acc
                  .setValue(TransportMeansIdentificationPage(Index(i)), identification)
                  .setValue(TransportMeansIdentificationNumberPage(Index(i)), identificationNumber)
            }

            val result = new AddAnotherTransportMeansViewModelProvider()(userAnswers, departureId, mode)
            result.listItems.length mustBe departureTransportMeans
            result.title mustBe s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
            result.heading mustBe s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
            result.legend mustBe "Do you want to add another departure means of transport?"
        }
      }

      "and inland mode is not Road" in {
        val formatter = java.text.NumberFormat.getIntegerInstance

        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], nonEmptyString, Gen.choose(2, frontendAppConfig.maxTransportMeans)) {
          (mode, identification, identificationNumber, departureTransportMeans) =>
            val userAnswers = (0 until departureTransportMeans).foldLeft(emptyUserAnswers) {
              (acc, i) =>
                acc
                  .setValue(TransportMeansIdentificationPage(Index(i)), identification)
                  .setValue(TransportMeansIdentificationNumberPage(Index(i)), identificationNumber)
            }

            val result = new AddAnotherTransportMeansViewModelProvider()(userAnswers, departureId, mode)
            result.listItems.length mustBe departureTransportMeans
            result.title mustBe s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
            result.heading mustBe s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
            result.legend mustBe "Do you want to add another departure means of transport?"
        }
      }
    }

    "with change and remove links" - {
      "when only one Departure transport mean and inland mode is Mail (5)" in {
        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], nonEmptyString) {
          (mode, identification, identificationNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, InlandMode(Mail, "test"))
              .setValue(TransportMeansIdentificationPage(Index(0)), identification)
              .setValue(TransportMeansIdentificationNumberPage(Index(0)), identificationNumber)

            val result = new AddAnotherTransportMeansViewModelProvider()(userAnswers, departureId, mode)

            result.listItems mustBe Seq(
              ListItem(
                name = s"${identification.asString} - $identificationNumber",
                changeUrl =
                  controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, Index(0)).url,
                removeUrl = Some(
                  controllers.transport.departureTransportMeans.routes.RemoveDepartureTransportMeansYesNoController.onPageLoad(departureId, mode, Index(0)).url
                )
              )
            )
        }
      }

      "when only one Departure transport mean and inland mode is not Mail (5)" in {
        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification], nonEmptyString) {
          (mode, identification, identificationNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, InlandMode(Air, "test"))
              .setValue(TransportMeansIdentificationPage(Index(0)), identification)
              .setValue(TransportMeansIdentificationNumberPage(Index(0)), identificationNumber)

            val result = new AddAnotherTransportMeansViewModelProvider().apply(userAnswers, departureId, mode)

            result.listItems mustBe Seq(
              ListItem(
                name = s"${identification.asString} - $identificationNumber",
                changeUrl =
                  controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController.onPageLoad(departureId, NormalMode, Index(0)).url,
                removeUrl = None
              )
            )
        }
      }
    }

  }
}
