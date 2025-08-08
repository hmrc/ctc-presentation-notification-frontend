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

package services

import base.SpecBase
import config.Constants.AdditionalDeclarationType
import connectors.DepartureMovementConnector
import generated.{CC013CType, CC015CType, CC170CType}
import generators.Generators
import models.departureP5.MessageType.*
import models.departureP5.{DepartureMessages, MessageMetaData}
import models.{MessageStatus, RichCC013CType}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureMessageServiceSpec extends SpecBase with Generators {

  private val mockConnector = mock[DepartureMovementConnector]
  private val service       = new DepartureMessageService(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "DepartureMessageService" - {

    "getLRN" in {
      when(mockConnector.getLRN(any())(any())).thenReturn(Future.successful(lrn))

      service.getLRN(departureId).futureValue mustEqual lrn

      verify(mockConnector).getLRN(eqTo(departureId))(any())
    }

    "getDepartureData" - {
      "must retrieve IE013 or IE015" - {
        "when IE013 is latest" in {
          forAll(arbitrary[CC013CType]) {
            ie013 =>
              beforeEach()

              val ie015MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now(), DeclarationData, "id1", MessageStatus.Success)

              val ie013MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now().plusDays(1), DeclarationAmendment, "id2", MessageStatus.Success)

              val departureMessages: DepartureMessages = DepartureMessages(List(ie015MetaData, ie013MetaData))

              when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
              when(mockConnector.getMessage[CC013CType](any(), any())(any(), any())).thenReturn(Future.successful(ie013))

              service.getDepartureData(departureId, lrn).futureValue.value mustEqual ie013.toCC015CType(lrn)

              verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
              verify(mockConnector).getMessage(eqTo(departureId), eqTo("id2"))(any(), any())
          }
        }

        "when IE015 is latest" in {
          forAll(arbitrary[CC015CType]) {
            ie015 =>
              beforeEach()

              val ie015MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now(), DeclarationData, "id1", MessageStatus.Success)

              val departureMessages: DepartureMessages = DepartureMessages(List(ie015MetaData))

              when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
              when(mockConnector.getMessage[CC015CType](any(), any())(any(), any())).thenReturn(Future.successful(ie015))

              service.getDepartureData(departureId, lrn).futureValue.value mustEqual ie015

              verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
              verify(mockConnector).getMessage(eqTo(departureId), eqTo("id1"))(any(), any())
          }
        }
      }
    }

    "getIE170" - {
      "must get IE170" in {
        forAll(arbitrary[CC170CType]) {
          ie170 =>
            beforeEach()

            val ie170MetaData: MessageMetaData =
              MessageMetaData(LocalDateTime.now(), PresentationForThePreLodgedDeclaration, "id1", MessageStatus.Success)

            val departureMessages: DepartureMessages = DepartureMessages(List(ie170MetaData))

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
            when(mockConnector.getMessage[CC170CType](any(), any())(any(), any())).thenReturn(Future.successful(ie170))

            service.getIE170(departureId).futureValue.value mustEqual ie170

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
            verify(mockConnector).getMessage(eqTo(departureId), eqTo("id1"))(any(), any())
        }
      }
    }

    "canSubmitPresentationNotification" - {
      "must return false" - {
        "when IE015/IE013 has a standard (A) additional declaration type" in {
          val result = service.canSubmitPresentationNotification(departureId, lrn, AdditionalDeclarationType.Standard).futureValue

          result mustEqual false

          verifyNoInteractions(mockConnector)
        }

        "when IE013/IE015 has a pre-lodged (D) additional declaration type" - {
          "and head message is not one of IE928, IE004, IE060" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, AdditionalDeclarationType.PreLodged).futureValue

            result.mustEqual(false)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }
        }
      }

      "must return true" - {
        "when IE013/IE015 has a pre-lodged (D) additional declaration type" - {

          val additionalDeclarationType = AdditionalDeclarationType.PreLodged

          "and head message is IE928" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success),
                MessageMetaData(time.plusDays(1), PositiveAcknowledgement, "2", MessageStatus.Success)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType).futureValue

            result.mustEqual(true)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }

          "and head message is IE004" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success),
                MessageMetaData(time.plusDays(1), PositiveAcknowledgement, "2", MessageStatus.Success),
                MessageMetaData(time.plusDays(2), DeclarationAmendment, "3", MessageStatus.Success),
                MessageMetaData(time.plusDays(3), AmendmentAcceptance, "4", MessageStatus.Success)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType).futureValue

            result.mustEqual(true)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }

          "and head message is IE060" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success),
                MessageMetaData(time.plusDays(1), PositiveAcknowledgement, "2", MessageStatus.Success),
                MessageMetaData(time.plusDays(2), ControlDecisionNotification, "3", MessageStatus.Success)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType).futureValue

            result.mustEqual(true)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }

          "and head message is failed IE170" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success),
                MessageMetaData(time.plusDays(1), PositiveAcknowledgement, "2", MessageStatus.Success),
                MessageMetaData(time.plusDays(2), PresentationForThePreLodgedDeclaration, "3", MessageStatus.Failed)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType).futureValue

            result.mustEqual(true)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }

          "and head message is IE056" in {
            val time = LocalDateTime.now()

            val messages = DepartureMessages(
              List(
                MessageMetaData(time, DeclarationData, "1", MessageStatus.Success),
                MessageMetaData(time.plusDays(1), PositiveAcknowledgement, "2", MessageStatus.Success),
                MessageMetaData(time.plusDays(2), PresentationForThePreLodgedDeclaration, "3", MessageStatus.Success),
                MessageMetaData(time.plusDays(3), RejectionFromOfficeOfDeparture, "4", MessageStatus.Success)
              )
            )

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(messages))

            val result = service.canSubmitPresentationNotification(departureId, lrn, additionalDeclarationType).futureValue

            result.mustEqual(true)

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
          }
        }
      }
    }
  }
}
