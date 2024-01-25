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
import forms.EnumerableFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import models.reference.TransportMode.InlandMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.transport.InlandModePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TransportModeCodesService
import views.html.transport.InlandModeView

import scala.concurrent.Future

class InlandModeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val inland1: InlandMode = InlandMode("1", "A2GjAWj")
  val inland2: InlandMode = InlandMode("2", "jn58227")

  private val inlandModes: Seq[InlandMode] = Seq(inland1, inland2)

  private val formProvider                    = new EnumerableFormProvider()
  private val form                            = formProvider[InlandMode]("transport.inlandModeOfTransport", inlandModes)
  private val mode                            = NormalMode
  private lazy val inlandModeOfTransportRoute = controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode).url

  private val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportModeCodesService]).toInstance(mockTransportModeCodesService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTransportModeCodesService)
    when(mockTransportModeCodesService.getInlandModes()(any())).thenReturn(Future.successful(inlandModes))
  }

  "InlandMode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, inlandModeOfTransportRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, inlandModes, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered in the IE015" in {
      when(mockTransportModeCodesService.getInlandModes()(any())).thenReturn(Future.successful(inlandModes))
      val userAnswers15 = UserAnswers.setInlandModeOfTransportOnUserAnswersLens.set(Some(inlandModes.head.code))(emptyUserAnswers)
      setExistingUserAnswers(userAnswers15)

      val request = FakeRequest(GET, inlandModeOfTransportRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> inlandModes.head.code))

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(filledForm, departureId, inlandModes, mode)(request, messages).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(InlandModePage, inlandModes.head)
      setExistingUserAnswers(userAnswers)

      when(mockTransportModeCodesService.getInlandModes()(any())).thenReturn(Future.successful(inlandModes))
      val request = FakeRequest(GET, inlandModeOfTransportRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> inlandModes.head.code))

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(filledForm, departureId, inlandModes, mode)(request, messages).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, inlandModeOfTransportRoute)
        .withFormUrlEncodedBody(("value", inlandModes.head.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, inlandModeOfTransportRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, inlandModes, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, inlandModeOfTransportRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, inlandModeOfTransportRoute)
        .withFormUrlEncodedBody(("value", inlandModes.head.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
