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
import forms.EnumerableFormProvider
import generators.Generators
import models.{LocationType, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.locationOfGoods.{InferredLocationTypePage, LocationTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.LocationTypeService
import views.html.locationOfGoods.LocationTypeView

import scala.concurrent.Future

class LocationTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private lazy val locationTypeRoute = controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode).url

  private val lts: Seq[LocationType] =
    Gen.containerOfN[Seq, LocationType](2, arbitrary[LocationType]).sample.value
  private val lt                                           = lts.head
  private val formProvider                                 = new EnumerableFormProvider()
  private val form                                         = formProvider("locationOfGoods.locationType", lts)
  private val mode                                         = NormalMode
  private val mockLocationTypeService: LocationTypeService = mock[LocationTypeService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLocationTypeService)
    when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))
  }

  override protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[LocationTypeService]).toInstance(mockLocationTypeService))

  "LocationType Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, lts, mode)(request, messages).toString
    }

    "must redirect to the next page and infer LocationType when only one location type" in {

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(Seq(lt)))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.getValue(InferredLocationTypePage) mustBe lt
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(LocationTypePage, lt)

      setExistingUserAnswers(userAnswers)

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> lt.code))

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, lts, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))

      val request   = FakeRequest(POST, locationTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, lts, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

  }
}
