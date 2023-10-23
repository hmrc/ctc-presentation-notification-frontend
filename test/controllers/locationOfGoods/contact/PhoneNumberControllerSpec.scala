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

package controllers.locationOfGoods.contact

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import controllers.locationOfGoods.contact.{routes => contactRoutes}
import forms.TelephoneNumberFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.locationOfGoods.contact.PhoneNumberView

import scala.concurrent.Future

class PhoneNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider              = new TelephoneNumberFormProvider()
  private val contactName               = "Contact"
  private val form                      = formProvider("locationOfGoods.contactPhoneNumber", contactName)
  private val mode                      = NormalMode
  private lazy val telephoneNumberRoute = contactRoutes.PhoneNumberController.onPageLoad(departureId, mode).url
  private val validAnswer: String       = "+123123"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "TelephoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers.setValue(NamePage, contactName)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[PhoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, contactName, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(NamePage, contactName)
        .setValue(PhoneNumberPage, validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[PhoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, contactName, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(NamePage, contactName)
      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, telephoneNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(NamePage, contactName)
      setExistingUserAnswers(userAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, telephoneNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[PhoneNumberView]

      contentAsString(result) mustEqual
        view(filledForm, departureId, contactName, mode)(request, messages).toString
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
