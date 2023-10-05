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
import forms.UnLocodeFormProvider
import models.reference.UnLocode
import models.{NormalMode, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.UnLocodePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UnLocodeService
import views.html.locationOfGoods.UnLocodeView

import scala.concurrent.Future

class UnLocodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val unLocode1                                        = UnLocode("ABC", "val 1")
  private val unLocode2                                        = UnLocode("DEF", "val 2")
  private val unLocodeSelectableList: SelectableList[UnLocode] = SelectableList.apply(Seq(unLocode1, unLocode2))

  private val formProvider                         = new UnLocodeFormProvider()
  private val form                                 = formProvider("locationOfGoods.unLocode", unLocodeSelectableList)
  private val mode                                 = NormalMode
  private lazy val unLocodeRoute                   = controllers.locationOfGoods.routes.UnLocodeController.onPageLoad(departureId, mode).url
  private val mockUnLocodeService: UnLocodeService = mock[UnLocodeService]

  override protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[UnLocodeService]).toInstance(mockUnLocodeService))

  override def beforeEach(): Unit = {
    reset(mockUnLocodeService)
    super.beforeEach()
  }

  "UnLocode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.getUnLocodeList(any())).thenReturn(Future.successful(unLocodeSelectableList))

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, unLocodeSelectableList.values, departureId, lrn.toString, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(UnLocodePage, unLocode1)

      setExistingUserAnswers(userAnswers)

      when(mockUnLocodeService.getUnLocodeList(any())).thenReturn(Future.successful(unLocodeSelectableList))

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> unLocode1.value))

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, unLocodeSelectableList.values, departureId, lrn.value, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.getUnLocodeList(any())).thenReturn(Future.successful(unLocodeSelectableList))

      val request = FakeRequest(POST, unLocodeRoute)
        .withFormUrlEncodedBody(("value", unLocode1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockUnLocodeService.getUnLocodeList(any())).thenReturn(Future.successful(unLocodeSelectableList))

      val request   = FakeRequest(POST, unLocodeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[UnLocodeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, unLocodeSelectableList.values, departureId, lrn.value, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, unLocodeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, unLocodeRoute)
        .withFormUrlEncodedBody(("value", unLocode1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
