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

package controllers.houseConsignment.index.departureTransportMeans

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.EnumerableFormProvider
import generators.Generators
import models.NormalMode
import models.reference.TransportMode.BorderMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.departureTransportMeans.IdentificationPage
import pages.transport.border.BorderModeOfTransportPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TransportMeansIdentificationTypesService
import views.html.houseConsignment.index.departureTransportMeans.IdentificationView

import scala.concurrent.Future

class IdentificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val identificationType1 = TransportMeansIdentification("40", "IATA flight number")
  private val identificationType2 = TransportMeansIdentification("41", "Registration number of an aircraft")
  private val identificationTypes = Seq(identificationType1, identificationType2)

  private val formProvider = new EnumerableFormProvider()
  private val form         = formProvider[TransportMeansIdentification]("houseConsignment.index.departureTransportMeans.identification", identificationTypes)
  private val mode         = NormalMode

  private lazy val identificationRoute = controllers.houseConsignment.index.departureTransportMeans.routes.IdentificationController
    .onPageLoad(departureId, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)
    .url

  private val mockTransportMeansIdentificationTypesService: TransportMeansIdentificationTypesService =
    mock[TransportMeansIdentificationTypesService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTransportMeansIdentificationTypesService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansIdentificationTypesService]).toInstance(mockTransportMeansIdentificationTypesService))

  "Identification Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockTransportMeansIdentificationTypesService.getMeansOfTransportIdentificationTypes(any(), any())(any(), any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, identificationTypes, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockTransportMeansIdentificationTypesService.getMeansOfTransportIdentificationTypes(any(), any())(any(), any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
        .setValue(IdentificationPage(houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex), identificationType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> identificationType1.code))

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, identificationTypes, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)(request,
                                                                                                                                      messages
        ).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)
      when(mockTransportMeansIdentificationTypesService.getMeansOfTransportIdentificationTypes(any(), any())(any(), any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identificationType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockTransportMeansIdentificationTypesService.getMeansOfTransportIdentificationTypes(any(), any())(any(), any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, identificationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, identificationTypes, mode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identificationType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
