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

package utils

import base.SpecBase
import generators.Generators
import models.Mode
import models.reference.CustomsOffice
import models.reference.TransportMode.BorderMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import pages.transport.{ContainerIndicatorPage, LimitDatePage}

import java.time.LocalDate
import scala.concurrent.Future

class PresentationNotificationAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "PresentationNotificationAnswersHelper" - {

    "customs office departure reference" - {

      "must return Some(Row)" in {
        forAll(arbitrary[Mode], nonEmptyString) {
          (mode, departureCustomsOfficeRefNumber) =>
            val userAnswers = setCustomsOfficeDepartureReferenceLens.set(departureCustomsOfficeRefNumber)(emptyUserAnswers)
            val helper      = new PresentationNotificationAnswersHelper(userAnswers, departureId, mode)
            val result      = helper.customsOfficeDeparture.get

            result.key.value mustBe s"Office of departure"
            result.value.value mustBe departureCustomsOfficeRefNumber
            val actions = result.actions
            actions.size mustBe 0
        }
      }
    }

    "limitDate" - {
      "must return None when no limit date in ie15/170" - {
        s"when $LimitDatePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PresentationNotificationAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.limitDate
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LimitDatePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(customsOffice))
              val limitDate = LocalDate.of(2000: Int, 1: Int, 8: Int)
              val answers = emptyUserAnswers
                .setValue(LimitDatePage, limitDate)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.limitDate.get

              result.key.value mustBe s"Estimated arrival date at the office of destination"
              result.value.value mustBe "8 January 2000"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.LimitDateController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "estimated arrival date at the office of destination"
              action.id mustBe "change-limit-date"
          }
        }
      }
    }

    "containerIndicator" - {
      "must return None when no container indicator in ie15/170" - {
        s"when $ContainerIndicatorPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PresentationNotificationAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.containerIndicator
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIndicatorPage defined in the ie170" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.containerIndicator.get

              result.key.value mustBe s"Are you using any shipping containers to transport the goods?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.ContainerIndicatorController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you are using any shipping containers to transport the goods"
              action.id mustBe "change-container-indicator"
          }
        }
      }
    }

    "addBorderModeOfTransportYesNo" - {
      "must return No" - {
        "when transport mode is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddBorderModeOfTransportYesNoPage, false)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.borderModeOfTransportYesNo
              result.get.key.value mustBe s"Do you want to add a border mode of transport?"
              result.get.value.value mustBe "No"
              val actions = result.get.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.AddBorderModeOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add a border mode of transport"
              action.id mustBe "change-add-border-mode"
          }
        }
      }
    }

    "borderModeOfTransport" - {
      "must return Some(Row)" - {
        s"when ModeCrossingBorderPage defined in the ie170" in {

          forAll(arbitrary[Mode], arbitrary[BorderMode]) {
            (mode, borderModeOfTransport) =>
              val answers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.borderModeOfTransportRow.get

              result.key.value mustBe s"Mode"
              result.value.value mustBe borderModeOfTransport.description
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "border mode of transport"
              action.id mustBe "change-border-mode-of-transport"
          }
        }
      }

    }

  }
}
