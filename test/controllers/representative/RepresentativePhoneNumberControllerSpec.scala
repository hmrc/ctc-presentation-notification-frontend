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

package controllers.representative

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.{representative, routes}
import forms.TelephoneNumberFormProvider
import models.messages.{ContactPerson, Representative}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.representative.RepresentativePhoneNumberPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.representative.RepresentativePhoneNumberView

import scala.concurrent.Future

class RepresentativePhoneNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider              = new TelephoneNumberFormProvider()
  private val form                      = formProvider("representative.representativeTelephoneNumber")
  private val mode                      = NormalMode
  private lazy val telephoneNumberRoute = representative.routes.RepresentativePhoneNumberController.onPageLoad(departureId, mode).url
  private val validAnswer: String       = "+44 808 157 0192"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RepresentativePhoneNumberController" - {

    "must return OK and the correct view for a GET when unanswered in IE015/013" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[RepresentativePhoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered in IE015/013" in {

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(RepresentativePhoneNumberPage, validAnswer)
      )

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[RepresentativePhoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, telephoneNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, telephoneNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RepresentativePhoneNumberView]

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, telephoneNumberRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
