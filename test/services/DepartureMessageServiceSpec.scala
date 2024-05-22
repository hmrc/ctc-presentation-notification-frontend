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
import connectors.DepartureMovementConnector
import generated.{CC013CType, CC015CType, CC170CType}
import generators.Generators
import models.RichCC013CType
import models.departureP5.MessageType.{DeclarationAmendment, DeclarationData, PresentationForThePreLodgedDeclaration}
import models.departureP5.{DepartureMessages, MessageMetaData}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureMessageServiceSpec extends SpecBase with Generators with BeforeAndAfterEach {

  private val mockConnector = mock[DepartureMovementConnector]
  private val service       = new DepartureMessageService(mockConnector)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMovementConnector].toInstance(mockConnector))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "DepartureMessageService" - {

    "getLRN" in {
      when(mockConnector.getLRN(any())(any())).thenReturn(Future.successful(lrn))

      service.getLRN(departureId).futureValue mustBe lrn

      verify(mockConnector).getLRN(eqTo(departureId))(any())
    }

    "getDepartureData" - {
      "must retrieve IE013 or IE015" - {
        "when IE013 is latest" in {
          forAll(arbitrary[CC013CType]) {
            ie013 =>
              beforeEach()

              val ie015MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now(), DeclarationData, "id1")

              val ie013MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now().plusDays(1), DeclarationAmendment, "id2")

              val departureMessages: DepartureMessages = DepartureMessages(List(ie015MetaData, ie013MetaData))

              when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
              when(mockConnector.getMessage[CC013CType](any(), any())(any(), any())).thenReturn(Future.successful(ie013))

              service.getDepartureData(departureId, lrn).futureValue.value mustBe ie013.toCC015CType(lrn)

              verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
              verify(mockConnector).getMessage(eqTo(departureId), eqTo("id2"))(any(), any())
          }
        }

        "when IE015 is latest" in {
          forAll(arbitrary[CC015CType]) {
            ie015 =>
              beforeEach()

              val ie015MetaData: MessageMetaData =
                MessageMetaData(LocalDateTime.now(), DeclarationData, "id1")

              val departureMessages: DepartureMessages = DepartureMessages(List(ie015MetaData))

              when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
              when(mockConnector.getMessage[CC015CType](any(), any())(any(), any())).thenReturn(Future.successful(ie015))

              service.getDepartureData(departureId, lrn).futureValue.value mustBe ie015

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
              MessageMetaData(LocalDateTime.now(), PresentationForThePreLodgedDeclaration, "id1")

            val departureMessages: DepartureMessages = DepartureMessages(List(ie170MetaData))

            when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
            when(mockConnector.getMessage[CC170CType](any(), any())(any(), any())).thenReturn(Future.successful(ie170))

            service.getIE170(departureId).futureValue.value mustBe ie170

            verify(mockConnector).getMessages(eqTo(departureId))(any(), any())
            verify(mockConnector).getMessage(eqTo(departureId), eqTo("id1"))(any(), any())
        }
      }
    }
  }
}
