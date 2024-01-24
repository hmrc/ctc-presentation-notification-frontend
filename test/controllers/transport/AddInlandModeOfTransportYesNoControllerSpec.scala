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

package controllers.transport

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.YesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.transport.AddInlandModeOfTransportYesNoPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.AddInlandModeOfTransportYesNoView

import scala.concurrent.Future

class AddInlandModeOfTransportYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider                            = new YesNoFormProvider()
  private val form                                    = formProvider("transport.addInlandModeOfTransport")
  private val mode                                    = NormalMode
  private lazy val addInlandModeOfTransportYesNoRoute = controllers.transport.routes.AddInlandModeOfTransportYesNoController.onPageLoad(departureId, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "addBorderMeansOfTransportYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(UserAnswers.setInlandModeOfTransportOnUserAnswersLens.set(None)(emptyUserAnswers))

      val request = FakeRequest(GET, addInlandModeOfTransportYesNoRoute)
      val result  = route(app, request).value

      val filledForm = form.bind(Map("value" -> "false"))

      val view = injector.instanceOf[AddInlandModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must return OK and the correct view for a GET when set in the IE015" in {

      setExistingUserAnswers(UserAnswers.setModeOfTransportAtTheBorderOnUserAnswersLens.set(Some("Air"))(emptyUserAnswers))

      val request = FakeRequest(GET, addInlandModeOfTransportYesNoRoute)
      val result  = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddInlandModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddInlandModeOfTransportYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addInlandModeOfTransportYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddInlandModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously  been answered in the IE015" in {

      val userAnswers15 = UserAnswers.setInlandModeOfTransportOnUserAnswersLens.set(Some("Air"))(emptyUserAnswers)
      setExistingUserAnswers(userAnswers15)

      val request = FakeRequest(GET, addInlandModeOfTransportYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddInlandModeOfTransportYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addInlandModeOfTransportYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, addInlandModeOfTransportYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddInlandModeOfTransportYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addInlandModeOfTransportYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addInlandModeOfTransportYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
