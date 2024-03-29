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

import base.TestMessageData.messageData
import base.SpecBase
import connectors.DepartureMovementConnector
import generators.Generators
import models.departureP5.MessageType.{AmendmentSubmitted, DepartureNotification}
import models.departureP5.{DepartureMessages, MessageMetaData}
import models.messages.Data
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureMessageServiceSpec extends SpecBase with Generators with BeforeAndAfterEach {

  private val mockConnector = mock[DepartureMovementConnector]
  private val service       = new DepartureMessageService(mockConnector)

  private val departureMessageMetaData1: MessageMetaData = MessageMetaData(LocalDateTime.now(), DepartureNotification, "path/url")

  private val departureMessageMetaData2: MessageMetaData =
    MessageMetaData(LocalDateTime.now().minusDays(1), AmendmentSubmitted, "path/url")

  private val departureMessages: DepartureMessages = DepartureMessages(List(departureMessageMetaData1, departureMessageMetaData2))

  private val ie015Data = Data(messageData)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMovementConnector].toInstance(mockConnector))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "DepartureMessageService" - {

    "getLRN success" in {
      when(mockConnector.getLRN(any())(any())).thenReturn(Future.successful(lrn))
      service.getLRN(departureId).futureValue mustBe lrn
      verify(mockConnector).getLRN(any())(any())
    }

    "getDepartureData success" in {
      when(mockConnector.getMessages(any())(any(), any())).thenReturn(Future.successful(departureMessages))
      when(mockConnector.getMessage(any(), any())(any())).thenReturn(Future.successful(ie015Data))
      service.getDepartureData(departureId).futureValue mustBe Some(ie015Data)
      verify(mockConnector).getMessage(any(), any())(any())
      verify(mockConnector).getMessages(any())(any(), any())
    }
  }
}
