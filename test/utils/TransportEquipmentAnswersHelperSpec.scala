/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import base.TestMessageData.allOptionsNoneJsonValue
import base.{SpecBase, TestMessageData}
import generators.Generators
import models.messages.MessageData
import models.{Mode, UserAnswers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import play.api.libs.json.Json
import services.CheckYourAnswersReferenceDataService
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant

class TransportEquipmentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService = mock[CheckYourAnswersReferenceDataService]

  "TransportEquipmentAnswersHelper" - {

    "addAnyTransportEquipmentYesNo" - {
      "must return No when TransportEquipment has not been answered in ie15/ie13" - {
        s"when $AddTransportEquipmentYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoAddBorderMeansOfTransportYesNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper =
                new TransportEquipmentAnswersHelper(ie015WithNoAddBorderMeansOfTransportYesNoUserAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.addAnyTransportEquipmentYesNo.get

              result.key.value mustBe "Do you want to add any transport equipment?"
              result.value.value mustBe "No"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add any transport equipment"
              action.id mustBe "change-add-transport-equipment"
          }
        }
      }

      "must return Yes when TransportEquipment has been answered in ie15/ie13" - {
        s"when $AddTransportEquipmentYesNoPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
            (mode, userAnswers) =>
              val helper = new TransportEquipmentAnswersHelper(userAnswers, departureId, refDataService, mode, activeIndex)
              val result = helper.addAnyTransportEquipmentYesNo.get

              result.key.value mustBe "Do you want to add any transport equipment?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.equipment.routes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add any transport equipment"
              action.id mustBe "change-add-transport-equipment"
          }
        }
      }
    }

  }
}
