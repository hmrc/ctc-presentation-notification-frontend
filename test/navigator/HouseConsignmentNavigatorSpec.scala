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
import base.TestMessageData.allOptionsNoneJsonValue
import controllers.houseConsignment.index.departureTransportMeans.routes
import generators.Generators
import models._
import models.messages.MessageData
import models.reference.transport.transportMeans.TransportMeansIdentification
import navigation.HouseConsignmentNavigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import pages.houseConsignment.index.departureTransportMeans._
import play.api.libs.json.Json

import java.time.Instant

class HouseConsignmentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new HouseConsignmentNavigator

  "HouseConsignmentNavigator" - {

    "in CheckMode" - {
      val mode = CheckMode
      "must go from AddDepartureTransportMeansYesNo" - {

        "to CYA page when No " in {

          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), false)
          navigator
            .nextPage(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), userAnswers, departureId, mode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

        "to IdentificationPage when Yes and there is no answer to DepartureTransportMeans" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)

          navigator
            .nextPage(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), userAnswers, departureId, mode)
            .mustBe(routes.IdentificationController.onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex))

        }

        "to CheckYourAnswers when Yes and there is an answer to departure transport means  in ie170/15/13" in {
          forAll(arbitraryTransportMeansIdentification.arbitrary, nonEmptyString, arbitraryNationality.arbitrary) {
            (identification, identificationNumber, nationality) =>
              val userAnswers = emptyUserAnswers
                .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
                .setValue(IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identification)
                .setValue(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identificationNumber)
                .setValue(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), nationality)

              navigator
                .nextPage(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), userAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

          }
        }

      }

      "must go from identification page" - {

        "to identificationNumberPage when identification number does not exist in the 15/13/170" in {

          val ie015WithNoIdentificationNumberUserAnswers =
            UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
          navigator
            .nextPage(
              IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex),
              ie015WithNoIdentificationNumberUserAnswers,
              departureId,
              CheckMode
            )
            .mustBe(
              routes.IdentificationNumberController.onPageLoad(departureId, CheckMode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
            )

        }

        "to CYA page when identification number does exist in the 15/13/170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
            .setValue(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), "identification number")
          navigator
            .nextPage(IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), userAnswers, departureId, CheckMode)
            .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

        }

      }

      "must go from identification number page" - {

        "to country page when country page does not exist in the 15/13/170" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
          navigator
            .nextPage(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), userAnswers, departureId, CheckMode)
            .mustBe(routes.CountryController.onPageLoad(departureId, CheckMode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex))

        }

        "to CYA page when country page does exist in the 15/13/170" in {

          forAll(arbitraryNationality.arbitrary) {
            country =>
              val userAnswers = emptyUserAnswers
                .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
                .setValue(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), country)
              navigator
                .nextPage(IdentificationNumberPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), userAnswers, departureId, CheckMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

        }
      }

      "must go from country page" - {

        "to CYA page" in {
          forAll(arbitraryNationality.arbitrary) {
            country =>
              val userAnswers = emptyUserAnswers
                .setValue(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex), true)
                .setValue(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), country)
              navigator
                .nextPage(CountryPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), userAnswers, departureId, CheckMode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }
      }

    }
  }

}
