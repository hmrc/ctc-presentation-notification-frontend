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

package controllers.transport.border

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import controllers.transport.border.{routes => borderRoutes}
import forms.YesNoFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.transport.border.AddBorderModeOfTransportYesNoPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.border.AddBorderModeOfTransportYesNoView

import scala.concurrent.Future

class AddBorderModeOfTransportYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider                            = new YesNoFormProvider()
  private val form                                    = formProvider("transport.border.addBorderModeOfTransport")
  private val mode                                    = NormalMode
  private lazy val addBorderModeOfTransportYesNoRoute = borderRoutes.AddBorderModeOfTransportYesNoController.onPageLoad(departureId, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "AddBorderModeOfTransportYesNoController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(setModeOfTransportAtTheBorderOnUserAnswersLens.replace(None)(emptyUserAnswers))

      val request = FakeRequest(GET, addBorderModeOfTransportYesNoRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[AddBorderModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddBorderModeOfTransportYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addBorderModeOfTransportYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddBorderModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addBorderModeOfTransportYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, addBorderModeOfTransportYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddBorderModeOfTransportYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addBorderModeOfTransportYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addBorderModeOfTransportYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
