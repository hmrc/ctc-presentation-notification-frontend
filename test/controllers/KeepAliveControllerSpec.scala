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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, status, GET, _}

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private def keepAliveRoute(departureId: Option[String]): String = routes.KeepAliveController.keepAlive(departureId).url

  "Keep alive controller" - {
    "touch mongo cache when lrn is available" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = route(app, FakeRequest(GET, keepAliveRoute(Some(departureId)))).value

      status(result) mustEqual NO_CONTENT

      verify(mockSessionRepository, times(1)).get(any())
      verify(mockSessionRepository, times(1)).set(any())
    }

    "not touch mongo cache when lrn is not available" in {
      val result = route(app, FakeRequest(GET, keepAliveRoute(None))).value

      status(result) mustEqual NO_CONTENT

      verify(mockSessionRepository, never()).get(any())
      verify(mockSessionRepository, never()).set(any())
    }

    "return NO_CONTENT when get from mongo cache returns None" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val result = route(app, FakeRequest(GET, keepAliveRoute(Some(departureId)))).value

      status(result) mustEqual NO_CONTENT

      verify(mockSessionRepository, times(1)).get(any())
      verify(mockSessionRepository, never()).set(any())
    }

  }
}
