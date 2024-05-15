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
import forms.SelectableFormProvider
import generators.Generators
import models.reference.CustomsOffice
import models.{NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.border.active.CustomsOfficeActiveBorderPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CustomsOfficesService
import views.html.transport.border.active.CustomsOfficeActiveBorderView

import scala.concurrent.Future

class CustomsOfficeActiveBorderControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val exitOffice        = arbitrary[CustomsOffice].sample.value
  private val transitOffice     = arbitrary[CustomsOffice].sample.value
  private val destinationOffice = arbitrary[CustomsOffice].sample.value

  private val customOfficeList = List(destinationOffice, transitOffice, exitOffice)
  private val selectableList   = SelectableList(customOfficeList)

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("transport.border.active.customsOfficeActiveBorder", selectableList)
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]

  private lazy val customsOfficeActiveBorderRoute =
    routes.CustomsOfficeActiveBorderController.onPageLoad(departureId, mode, activeIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  "CustomsOfficeActiveBorder Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockCustomsOfficesService.getCustomsOfficesByMultipleIds(any())(any()))
        .thenReturn(Future.successful(customOfficeList))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, customsOfficeActiveBorderRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsOfficeActiveBorderView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, customOfficeList, mode, index)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCustomsOfficesService.getCustomsOfficesByMultipleIds(any())(any()))
        .thenReturn(Future.successful(customOfficeList))

      val userAnswers = emptyUserAnswers
        .setValue(CustomsOfficeActiveBorderPage(index), destinationOffice)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, customsOfficeActiveBorderRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> destinationOffice.id))

      val view = injector.instanceOf[CustomsOfficeActiveBorderView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, customOfficeList, mode, index)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesByMultipleIds(eqTo(customOfficeList.map(_.id)))(any()))
        .thenReturn(Future.successful(customOfficeList))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, customsOfficeActiveBorderRoute)
        .withFormUrlEncodedBody(("value", destinationOffice.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesByMultipleIds(eqTo(customOfficeList.map(_.id)))(any()))
        .thenReturn(Future.successful(customOfficeList))

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, customsOfficeActiveBorderRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsOfficeActiveBorderView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, departureId, customOfficeList, mode, index)(request, messages).toString
    }
  }

  "must redirect to Session Expired for a GET if no existing data is found" in {

    setNoExistingUserAnswers()

    val request = FakeRequest(GET, customsOfficeActiveBorderRoute)

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
  }

  "must redirect to Session Expired for a POST if no existing data is found" in {

    setNoExistingUserAnswers()

    val request = FakeRequest(POST, customsOfficeActiveBorderRoute)
      .withFormUrlEncodedBody(("value", destinationOffice.id))

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
  }

}
