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
import forms.SelectableFormProvider.CountryFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.transport.border.active.NationalityPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NationalitiesService
import views.html.transport.border.active.NationalityView

import scala.concurrent.Future

class NationalityControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val nationality1    = arbitraryNationality.arbitrary.sample.get
  private val nationality2    = arbitraryNationality.arbitrary.sample.get
  private val nationalityList = SelectableList(Seq(nationality1, nationality2))

  private val formProvider = new CountryFormProvider()
  private val form         = formProvider("transport.border.active.nationality", nationalityList)
  private val field        = formProvider.field

  private val mode = NormalMode

  private lazy val nationalityRoute = controllers.transport.border.active.routes.NationalityController.onPageLoad(departureId, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[NationalitiesService]).toInstance(mockNationalitiesService))

  "Nationality Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, nationalityRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[NationalityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, nationalityList.values, mode, index)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      val userAnswers = emptyUserAnswers.setValue(NationalityPage(index), nationality1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, nationalityRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> nationality1.code))

      val view = injector.instanceOf[NationalityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, nationalityList.values, mode, index)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, nationalityRoute)
        .withFormUrlEncodedBody((field, nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, nationalityRoute).withFormUrlEncodedBody((field, "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[NationalityView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, nationalityList.values, mode, index)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, nationalityRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, nationalityRoute)
        .withFormUrlEncodedBody((field, nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
