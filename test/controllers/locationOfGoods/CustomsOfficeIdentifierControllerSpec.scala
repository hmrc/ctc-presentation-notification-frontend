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

import base.{AppWithDefaultMockFixtures, SpecBase, TestMessageData}
import forms.SelectableFormProvider
import generators.Generators
import models.reference.CountryCode
import models.{NormalMode, SelectableList}
import navigation.Navigator
import navigation.annotations.LocationOfGoods
import navigator.FakeNavigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CustomsOfficesService
import views.html.locationOfGoods.CustomsOfficeIdentifierView

import scala.concurrent.Future

class CustomsOfficeIdentifierControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val customsOffice1    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOffice2    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOfficeList = SelectableList(Seq(customsOffice1, customsOffice2))

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]
  private lazy val customsOfficeIdentifierRoute                = routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[Navigator]).qualifiedWith(classOf[LocationOfGoods]).toInstance(fakeNavigator))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCustomsOfficesService)
  }

  private val countryCode   = arbitrary[CountryCode].sample.value
  private val departureData = TestMessageData.messageData.copy(CustomsOfficeOfDeparture = s"${countryCode.code}00001")
  private val baseAnswers   = emptyUserAnswers.copy(departureData = departureData)

  "CustomsOfficeIdentifier Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(baseAnswers)

      when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

      val request = FakeRequest(GET, customsOfficeIdentifierRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsOfficeIdentifierView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn.toString, departureId, customsOfficeList.values, mode)(request, messages).toString

      verify(mockCustomsOfficesService).getCustomsOfficesOfDepartureForCountry(eqTo(countryCode.code))(any())
    }
  }
  "must populate the view correctly on a GET when the question has previously been answered" in {

    when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

    val userAnswers = baseAnswers.setValue(CustomsOfficeIdentifierPage, customsOffice1)

    setExistingUserAnswers(userAnswers)

    val request = FakeRequest(GET, customsOfficeIdentifierRoute)

    val result = route(app, request).value

    val filledForm = form.bind(Map("value" -> customsOffice1.id))

    val view = injector.instanceOf[CustomsOfficeIdentifierView]

    status(result) mustEqual OK

    contentAsString(result) mustEqual
      view(filledForm, lrn.toString, departureId, customsOfficeList.values, mode)(request, messages).toString

    verify(mockCustomsOfficesService).getCustomsOfficesOfDepartureForCountry(eqTo(countryCode.code))(any())
  }

  "must redirect to the next page when valid data is submitted" in {

    when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    setExistingUserAnswers(baseAnswers)

    val request = FakeRequest(POST, customsOfficeIdentifierRoute)
      .withFormUrlEncodedBody(("value", customsOffice1.id))

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual onwardRoute.url

    verify(mockCustomsOfficesService).getCustomsOfficesOfDepartureForCountry(eqTo(countryCode.code))(any())
  }

  "must return a Bad Request and errors when invalid data is submitted" in {

    when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

    setExistingUserAnswers(baseAnswers)

    val request   = FakeRequest(POST, customsOfficeIdentifierRoute).withFormUrlEncodedBody(("value", "invalid value"))
    val boundForm = form.bind(Map("value" -> "invalid value"))

    val result = route(app, request).value

    val view = injector.instanceOf[CustomsOfficeIdentifierView]

    status(result) mustEqual BAD_REQUEST

    contentAsString(result) mustEqual
      view(boundForm, lrn.toString, departureId, customsOfficeList.values, mode)(request, messages).toString

    verify(mockCustomsOfficesService).getCustomsOfficesOfDepartureForCountry(eqTo(countryCode.code))(any())
  }

  "must redirect to Session Expired for a GET if no existing data is found" in {

    setNoExistingUserAnswers()

    val request = FakeRequest(GET, customsOfficeIdentifierRoute)

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
  }

  "must redirect to Session Expired for a POST if no existing data is found" in {

    setNoExistingUserAnswers()

    val request = FakeRequest(POST, customsOfficeIdentifierRoute)
      .withFormUrlEncodedBody(("value", customsOffice1.id))

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
  }

}
