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
            val userAnswers = setCustomsOfficeDepartureReferenceLens.replace(departureCustomsOfficeRefNumber)(emptyUserAnswers)
            val helper      = new PresentationNotificationAnswersHelper(userAnswers, departureId, mode)
            val result      = helper.customsOfficeDeparture.get

            result.key.value mustEqual s"Office of departure"
            result.value.value mustEqual departureCustomsOfficeRefNumber
            val actions = result.actions
            actions.size mustEqual 0
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
              result must not be defined
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

              result.key.value mustEqual s"Estimated arrival date at the office of destination"
              result.value.value mustEqual "8 January 2000"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.routes.LimitDateController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "estimated arrival date at the office of destination"
              action.id mustEqual "change-limit-date"
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
              result must not be defined
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

              result.key.value mustEqual s"Are you using any shipping containers to transport the goods?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.routes.ContainerIndicatorController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you are using any shipping containers to transport the goods"
              action.id mustEqual "change-container-indicator"
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
              result.get.key.value mustEqual s"Do you want to add a border mode of transport?"
              result.get.value.value mustEqual "No"
              val actions = result.get.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.routes.AddBorderModeOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a border mode of transport"
              action.id mustEqual "change-add-border-mode"
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

              result.key.value mustEqual s"Mode"
              result.value.value mustEqual borderModeOfTransport.description
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "border mode of transport"
              action.id mustEqual "change-border-mode-of-transport"
          }
        }
      }

    }

  }
}
