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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val nextPage = controllers.routes.MoreInformationController.onPageLoad(departureId).url

  "Index Controller" - {

    "moreInformation" - {
//      "must redirect to onward route for a GET when there are no UserAnswers and prepopulated data" in {
//        val request = FakeRequest(GET, routes.IndexController.index(departureId).url)
//
//        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
//        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
//
//        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//        val result = route(app, request).value
//
//        status(result) mustEqual SEE_OTHER
//
//        redirectLocation(result).value mustEqual nextPage
//
//        verify(mockSessionRepository).set(userAnswersCaptor.capture())
//
//        val expectedData                = Json.toJsObject(IE044Data.fromIE043Data(unloadingAction.messageData))
//
//        userAnswersCaptor.getValue.data mustBe expectedData
//      }

      "must redirect to onward route when there are UserAnswers" in {

        val request = FakeRequest(GET, routes.IndexController.index(departureId).url)

        when(mockDepartureMessageService.getLRN(any())(any(), any())) thenReturn Future.successful(lrn)

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual nextPage

        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
      }
    }
  }
}
