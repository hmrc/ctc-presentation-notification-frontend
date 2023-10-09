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

package controllers

import base.TestMessageData.{incompleteJsonValue, jsonValue}
import base.{AppWithDefaultMockFixtures, SpecBase, TestMessageData}
import generators.Generators
import models.UserAnswers
import models.messages.{Data, MessageData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureMessageService

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val indexRoute = routes.IndexController.index(departureId).url

  private lazy val withIncompleteDataNextPage = routes.MoreInformationController.onPageLoad(departureId).url

  private lazy val withCompleteDataNextPage = routes.CheckInformationController.onPageLoad(departureId).url

  private val mockDepartureMessageService: DepartureMessageService = mock[DepartureMessageService]

  override protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService)
      )

  "Index Controller" - {

    "index" - {
      "must redirect to onward route for a GET when there are no UserAnswers and prepopulated data" in {
        val request = FakeRequest(GET, indexRoute)

        setNoExistingUserAnswers()

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)
        when(mockDepartureMessageService.getDepartureData(any())(any(), any())) thenReturn Future.successful(Some(Data(jsonValue.as[MessageData])))

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual withCompleteDataNextPage

        val expectedData = JsObject.empty

        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue.data mustBe expectedData
      }

      "must redirect to onward route when there are UserAnswers" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, indexRoute)

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)
        when(mockDepartureMessageService.getDepartureData(any())(any(), any())) thenReturn Future.successful(Some(Data(jsonValue.as[MessageData])))

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual withCompleteDataNextPage

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.lrn mustBe lrn.value
        userAnswersCaptor.getValue.eoriNumber mustBe eoriNumber
        userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
      }

      "must redirect to the correct onward route when there are UserAnswers and complete message data" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, indexRoute)

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)
        when(mockDepartureMessageService.getDepartureData(any())(any(), any())) thenReturn Future.successful(Some(Data(jsonValue.as[MessageData])))

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual withCompleteDataNextPage

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.lrn mustBe lrn.value
        userAnswersCaptor.getValue.eoriNumber mustBe eoriNumber
        userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
      }

      "must redirect to the correct onward route when there are UserAnswers and incomplete message data" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, indexRoute)

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)
        when(mockDepartureMessageService.getDepartureData(any())(any(), any())) thenReturn Future.successful(
          Some(Data(incompleteJsonValue.as[MessageData]))
        )

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual withIncompleteDataNextPage

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.lrn mustBe lrn.value
        userAnswersCaptor.getValue.eoriNumber mustBe eoriNumber
        userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
      }

      "must redirect to the technical difficulties route when there is an issue retrieving the data" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, indexRoute)

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)
        when(mockDepartureMessageService.getDepartureData(any())(any(), any())) thenReturn Future.successful(
          None
        )

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
