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

package services.submission

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import org.mockito.Mockito.{reset, when}
import pages.transport.LimitDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.DateTimeService

import java.time.{LocalDate, LocalDateTime}

class SubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val service = app.injector.instanceOf[SubmissionService]

  private lazy val mockDateTimeService = mock[DateTimeService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DateTimeService].toInstance(mockDateTimeService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)

    when(mockDateTimeService.now)
      .thenReturn(LocalDateTime.of(2020, 1, 1, 9, 30, 0))
  }

  "messageSequence" - {
    "must create message sequence" - {
      "when GB office of departure" in {
        val result = service.messageSequence(eoriNumber, "GB00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.GB",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "CC170C"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC170C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }

      "when XI office of departure" in {
        val result = service.messageSequence(eoriNumber, "XI00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.XI",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "CC170C"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC170C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }
    }
  }

  "transitOperation" - {
    "must create transit operation" - {
      "when limit date defined" in {
        val userAnswers = emptyUserAnswers
          .setValue(LimitDatePage, LocalDate.of(2020, 1, 1))

        val reads  = service.transitOperation(userAnswers)
        val result = userAnswers.data.as[TransitOperationType24](reads)

        result mustBe TransitOperationType24(
          LRN = userAnswers.lrn,
          limitDate = Some(XMLCalendar("2020-01-01"))
        )
      }

      "when limit date undefined" in {
        val userAnswers = emptyUserAnswers

        val reads  = service.transitOperation(userAnswers)
        val result = userAnswers.data.as[TransitOperationType24](reads)

        result mustBe TransitOperationType24(
          LRN = userAnswers.lrn,
          limitDate = None
        )
      }
    }
  }
}
