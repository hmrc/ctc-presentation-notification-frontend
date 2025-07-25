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

package controllers.locationOfGoods

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.UnLocodeFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.locationOfGoods.UnLocodePage
import play.api.data.FormError
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UnLocodeService
import views.html.locationOfGoods.UnLocodeView

import scala.concurrent.Future

class UnLocodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val prefix = "locationOfGoods.unLocode"

  private lazy val unLocodeRoute                   = controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode).url
  private val formProvider                         = new UnLocodeFormProvider()
  private val form                                 = formProvider(prefix)
  private val mode                                 = NormalMode
  private val mockUnLocodeService: UnLocodeService = mock[UnLocodeService]

  override def beforeEach(): Unit = {
    reset(mockUnLocodeService)
    super.beforeEach()
  }

  override protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[UnLocodeService]).toInstance(mockUnLocodeService))

  "UnLocode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.doesUnLocodeExist(any())(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(UnLocodePage, "DEAAL")

      setExistingUserAnswers(userAnswers)

      when(mockUnLocodeService.doesUnLocodeExist(any())(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "DEAAL"))

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.doesUnLocodeExist(any())(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, unLocodeRoute)
        .withFormUrlEncodedBody(("value", "DEAAL"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.doesUnLocodeExist(any())(any())) `thenReturn` Future.successful(true)

      val invalidAnswer = "invalid value"

      val request   = FakeRequest(POST, unLocodeRoute).withFormUrlEncodedBody(("value", invalidAnswer))
      val boundForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode)(request, messages).toString
    }

    "must return a Bad Request and errors when unknown data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.doesUnLocodeExist(any())(any())) `thenReturn` Future.successful(false)

      val unknownAnswer = "ABCDE"

      val request   = FakeRequest(POST, unLocodeRoute).withFormUrlEncodedBody(("value", unknownAnswer))
      val boundForm = form.bind(Map("value" -> unknownAnswer)).withError(FormError("value", s"$prefix.error.not.exists"))

      val result = route(app, request).value

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, unLocodeRoute)
        .withFormUrlEncodedBody(("value", "DEAAL"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
