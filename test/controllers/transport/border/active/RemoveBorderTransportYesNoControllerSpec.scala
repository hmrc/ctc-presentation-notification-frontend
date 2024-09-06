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

package controllers.transport.border.active

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.YesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.sections.transport.border.BorderActiveSection
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.border.active.RemoveBorderTransportYesNoView

import scala.concurrent.Future

class RemoveBorderTransportYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("transport.border.active.removeBorderTransport")
  private val mode         = NormalMode

  private lazy val removeBorderTransportRoute =
    controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, activeIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveBorderTransportYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(BorderActiveSection(activeIndex), Json.obj()))

      val request = FakeRequest(GET, removeBorderTransportRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[RemoveBorderTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode, activeIndex)(request, messages).toString
    }

    "when yes submitted" - {
      "must redirect to add another border transport and remove border transport at specified index" in {
        when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(BorderActiveSection(activeIndex), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeBorderTransportRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController
          .onPageLoad(departureId, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(BorderActiveSection(activeIndex)) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another border transport and not remove border transport at specified index" in {
        val userAnswers = emptyUserAnswers.setValue(BorderActiveSection(activeIndex), Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeBorderTransportRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController
          .onPageLoad(departureId, mode)
          .url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(BorderActiveSection(activeIndex)) must be(defined)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(BorderActiveSection(activeIndex), Json.obj()))

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, removeBorderTransportRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController
        .onPageLoad(departureId, mode)
        .url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(BorderActiveSection(activeIndex), Json.obj()))

      val invalidAnswer = ""

      val request    = FakeRequest(POST, removeBorderTransportRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveBorderTransportYesNoView]

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode, activeIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeBorderTransportRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeBorderTransportRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
