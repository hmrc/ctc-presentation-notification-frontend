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
import generators.Generators
import models.*
import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import navigation.DepartureTransportMeansNavigator
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.departureTransportMeans.*

class DepartureTransportMeansNavigatorSpec extends SpecBase with Generators {

  val navigator = new DepartureTransportMeansNavigator

  "DepartureTransportMeansNavigator" - {

    "in NormalMode" - {
      val mode = NormalMode

      "TransportMeansIdentificationPage" - {

        "must go to TransportMeansIdentificationNumberPage" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationPage(transportIndex), TransportMeansIdentification("10", "test"))
                .removeValue(TransportMeansIdentificationNumberPage(transportIndex))

              navigator
                .nextPage(TransportMeansIdentificationPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController
                    .onPageLoad(departureId, mode, transportIndex)
                )
          }
        }
      }

      "TransportMeansIdentificationNumberPage" - {

        "must go to TransportMeansNationalityPage" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), "test")
                .removeValue(TransportMeansNationalityPage(transportIndex))

              navigator
                .nextPage(TransportMeansIdentificationNumberPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController.onPageLoad(departureId, mode, transportIndex)
                )
          }
        }
      }

      "Must go from TransportMeansNationalityPage to add another page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansNationalityPage(transportIndex), Nationality("UK", "test"))
            navigator
              .nextPage(TransportMeansNationalityPage(transportIndex), updatedAnswers, departureId, mode)
              .mustEqual(controllers.transport.departureTransportMeans.routes.AddAnotherTransportMeansController.onPageLoad(departureId, mode))
        }
      }
    }

    "in CheckMode" - {
      val mode = CheckMode

      "TransportMeansIdentificationPage" - {

        "must go to TransportMeansIdentificationNumberPage when TransportMeansIdentificationNumberPage is empty" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationPage(transportIndex), TransportMeansIdentification("10", "test"))
                .removeValue(TransportMeansIdentificationNumberPage(transportIndex))

              navigator
                .nextPage(TransportMeansIdentificationPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController
                    .onPageLoad(departureId, mode, transportIndex)
                )
          }
        }

        "must go to CheckYourAnswers when TransportMeansIdentificationNumberPage is not empty" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationPage(transportIndex), TransportMeansIdentification("10", "test"))
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), "test")
              navigator
                .nextPage(TransportMeansIdentificationPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

      "TransportMeansIdentificationNumberPage" - {

        "must go to TransportMeansNationalityPage when TransportMeansNationalityPage is empty" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), "test")
                .removeValue(TransportMeansNationalityPage(transportIndex))

              navigator
                .nextPage(TransportMeansIdentificationNumberPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(
                  controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController.onPageLoad(departureId, mode, transportIndex)
                )
          }
        }

        "must go to CheckYourAnswers when TransportMeansNationalityPage is not empty" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), "test")
                .setValue(TransportMeansNationalityPage(transportIndex), Nationality("GB", "test"))
              navigator
                .nextPage(TransportMeansIdentificationNumberPage(transportIndex), updatedAnswers, departureId, mode)
                .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

      "Must go from TransportMeansNationalityPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansNationalityPage(transportIndex), Nationality("UK", "test"))
            navigator
              .nextPage(TransportMeansNationalityPage(transportIndex), updatedAnswers, departureId, mode)
              .mustEqual(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      }
    }
  }
}
