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

package controllers.transport.transportMeans

import base.TestMessageData.activeBorderTransportMeans
import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.SelectableFormProvider
import generators.Generators
import models.{NormalMode, SelectableList, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.transport.transportMeans.TransportMeansNationalityPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.NationalitiesService
import views.html.transport.transportMeans.TransportMeansNationalityView

import scala.concurrent.Future

class TransportMeansNationalityControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val nationality1    = arbitraryNationality.arbitrary.sample.get
  private val nationality2    = arbitraryNationality.arbitrary.sample.get
  private val nationalityList = SelectableList(Seq(nationality1, nationality2))

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("transport.transportMeans.nationality", nationalityList)
  private val mode         = NormalMode

  private val mockNationalitiesService: NationalitiesService = mock[NationalitiesService]
  private lazy val nationalityRoute                          = controllers.transport.transportMeans.routes.TransportMeansNationalityController.onPageLoad(departureId, mode).url

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

      val view = injector.instanceOf[TransportMeansNationalityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, nationalityList.values, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      val userAnswers = emptyUserAnswers.setValue(TransportMeansNationalityPage, nationality1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, nationalityRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> nationality1.code))

      val view = injector.instanceOf[TransportMeansNationalityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, nationalityList.values, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered in the IE015" in {
      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))

      val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(
        Option(Seq(activeBorderTransportMeans.head.copy(nationality = Some(nationality1.code))))
      )(emptyUserAnswers)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, nationalityRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> nationality1.code))

      val view = injector.instanceOf[TransportMeansNationalityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, nationalityList.values, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, nationalityRoute)
        .withFormUrlEncodedBody(("value", nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, nationalityRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[TransportMeansNationalityView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, nationalityList.values, mode)(request, messages).toString
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
        .withFormUrlEncodedBody(("value", nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
