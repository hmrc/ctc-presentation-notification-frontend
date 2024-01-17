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
import models._
import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import navigation.DepartureTransportMeansNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage, TransportMeansNationalityPage}

class DepartureTransportMeansNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new DepartureTransportMeansNavigator

  "BorderNavigator" - {

    "in Normal mode" - {
      val mode = NormalMode

      "Must go from TransportMeansIdentificationPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansIdentificationPage, TransportMeansIdentification("10", "test"))
            navigator
              .nextPage(TransportMeansIdentificationPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }
      "Must go from TransportMeansIdentificationNumberPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansIdentificationNumberPage, "test")
            navigator
              .nextPage(TransportMeansIdentificationNumberPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }
      "Must go from TransportMeansNationalityPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansNationalityPage, Nationality("UK", "test"))
            navigator
              .nextPage(TransportMeansNationalityPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }

    }

    "in CheckMode" - {
      val mode = CheckMode

      "Must go from TransportMeansIdentificationPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansIdentificationPage, TransportMeansIdentification("10", "test"))
            navigator
              .nextPage(TransportMeansIdentificationPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }
      "Must go from TransportMeansIdentificationNumberPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansIdentificationNumberPage, "test")
            navigator
              .nextPage(TransportMeansIdentificationNumberPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }
      "Must go from TransportMeansNationalityPage to check answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .setValue(TransportMeansNationalityPage, Nationality("UK", "test"))
            navigator
              .nextPage(TransportMeansNationalityPage, updatedAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }

    }
  }

}
