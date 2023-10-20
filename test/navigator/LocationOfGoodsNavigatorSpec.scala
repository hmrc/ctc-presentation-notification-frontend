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
import config.Constants._
import generators.Generators
import models._
import models.messages.MessageData
import navigation.LocationOfGoodsNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.{EoriPage, IdentificationPage, InferredLocationTypePage, LocationTypePage}

class LocationOfGoodsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new LocationOfGoodsNavigator

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode
      "must go from LocationTypePage to IdentificationPage" - {

        "when value is inferred" in {
          val value = arbitrary[LocationType].sample.value

          val userAnswers = emptyUserAnswers.setValue(InferredLocationTypePage, value)
          navigator
            .nextPage(InferredLocationTypePage, userAnswers, departureId, mode)
            .mustBe(IdentificationPage.route(userAnswers, departureId, mode).value)
        }

        "when value is not inferred" in {
          val value = arbitrary[LocationType].sample.value

          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, value)
          navigator
            .nextPage(LocationTypePage, userAnswers, departureId, mode)
            .mustBe(IdentificationPage.route(userAnswers, departureId, mode).value)
        }
      }

      "must go from IdentificationPage to next page" - {
        Seq[String](
          CustomsOfficeIdentifier,
          EoriNumberIdentifier,
          AuthorisationNumberIdentifier,
          UnlocodeIdentifier,
          CoordinatesIdentifier,
          AddressIdentifier,
          PostalCodeIdentifier
        ) foreach (
          identifier =>
            s"when value is $identifier" in {
              val value: LocationOfGoodsIdentification = LocationOfGoodsIdentification(identifier, "identifier")

              val userAnswers = emptyUserAnswers.setValue(IdentificationPage, value)
              navigator
                .nextPage(IdentificationPage, userAnswers, departureId, mode)
                .mustBe(navigator.routeIdentificationPageNavigation(userAnswers, departureId, mode).value)
            }
        )
      }

      "redirect to LocationTypeController when locationOfGoods is None and not simplified" in {

        val userAnswers                = arbitraryUserData.arbitrary.sample.value
        val consignment                = userAnswers.departureData.Consignment.copy(LocationOfGoods = None)
        val departureData: MessageData = userAnswers.departureData.copy(Authorisation = None, Consignment = consignment)
        val simplifiedUserAnswers      = userAnswers.copy(departureData = departureData)

        val result = navigator.locationOfGoodsNavigation(simplifiedUserAnswers, departureId, mode).get
        result.mustBe(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode))
      }

      "redirect to AuthorisationNumberController when locationOfGoods is None and is simplified" in {

        val userAnswers                = arbitraryUserData.arbitrary.sample.value
        val consignment                = userAnswers.departureData.Consignment.copy(LocationOfGoods = None)
        val departureData: MessageData = userAnswers.departureData.copy(Consignment = consignment)
        val simplifiedUserAnswers      = userAnswers.copy(departureData = departureData)

        val result = navigator.locationOfGoodsNavigation(simplifiedUserAnswers, departureId, mode).get
        result.mustBe(controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode))
      }

      "must go from EORI Page to Add Additional Identifier Yes No page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EoriPage, answers, departureId, NormalMode)
              .mustBe(controllers.locationOfGoods.routes.AddIdentifierYesNoController.onPageLoad(departureId, NormalMode))
        }
      }
    }
  }
}
