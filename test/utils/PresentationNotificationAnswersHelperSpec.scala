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
import base.TestMessageData.{allOptionsNoneJsonValue, messageData}
import generators.Generators
import models.messages.MessageData
import models.reference.CustomsOffice
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.libs.json.Json

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class PresentationNotificationAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "PresentationNotificationAnswersHelper" - {

    "limitDate" - {
      "must return None when no limit date in ie15/170" - {
        s"when $LimitDatePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoLimitDateUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLimitDateUserAnswers, departureId, mode)
              val result = helper.limitDate
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LimitDatePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(Some(customsOffice)))
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

        s"when $LimitDatePage defined in the ie15" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(Some(customsOffice)))
              val ie015WithLimitDateUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                        = new PresentationNotificationAnswersHelper(ie015WithLimitDateUserAnswers, departureId, mode)
              val result                        = helper.limitDate.get

              result.key.value mustBe s"Estimated arrival date at the office of destination"
              result.value.value mustBe "9 June 2023"
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
              val ie015WithNoLimitDateUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLimitDateUserAnswers, departureId, mode)
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

        s"when $ContainerIndicatorPage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithContainerIndicatorUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                                 = new PresentationNotificationAnswersHelper(ie015WithContainerIndicatorUserAnswers, departureId, mode)
              val result                                 = helper.containerIndicator.get

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

  }
}
