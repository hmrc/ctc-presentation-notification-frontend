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
import controllers.representative.{routes => representativeRoutes}
import controllers.routes
import forms.EoriNumberFormProvider
import models.messages.Representative
import models.{NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.representative.EoriPage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.representative.EoriView

class EoriControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider                 = new EoriNumberFormProvider()
  private val form                         = formProvider("representative.eori")
  private val mode                         = NormalMode
  private lazy val representativeEoriRoute = representativeRoutes.EoriController.onPageLoad(departureId, mode).url

  private lazy val validAnswer = eoriNumber.value

  override protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  override def beforeEach(): Unit =
    super.beforeEach()

  "Eori Controller" - {

    "must return OK and the correct view for a GET when Representative Section is unanswered in IE015/013" in {

      setExistingUserAnswers(UserAnswers.setRepresentativeOnUserAnswersLens.set(None)(emptyUserAnswers))

      val request = FakeRequest(GET, representativeEoriRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[EoriView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode)(request, messages).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered IE015/013" in {
      val uaIE015 = UserAnswers.setRepresentativeOnUserAnswersLens.set(Option(Representative("IdNumber", "2", None)))(emptyUserAnswers)

      setExistingUserAnswers(uaIE015)

      val request = FakeRequest(GET, representativeEoriRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "IdNumber"))

      val view = injector.instanceOf[EoriView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode)(request, messages).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, representativeEoriRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, representativeEoriRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[EoriView]

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode)(request, messages).toString

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, representativeEoriRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, representativeEoriRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

    }

  }
}
