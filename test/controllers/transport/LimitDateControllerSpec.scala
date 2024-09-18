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
import forms.transport.DateFormProvider
import models.NormalMode
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.transport.LimitDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CustomsOfficesService, DateTimeService}
import utils.RichLocalDate
import views.html.transport.LimitDateView

import java.time.LocalDate
import scala.concurrent.Future

class LimitDateControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val dateTimeService = injector.instanceOf[DateTimeService]

  private val minDate    = dateTimeService.currentDatePlusMinusDays(frontendAppConfig.limitDateDaysBefore)
  private val maxDate    = dateTimeService.currentDatePlusMinusDays(frontendAppConfig.limitDateDaysAfter)
  private val maxDateArg = maxDate.plusDays(1).formatForText

  private val formProvider        = new DateFormProvider()
  private val form                = formProvider("transport.limit.date", minDate, maxDate)
  private val mode                = NormalMode
  private lazy val limitDateRoute = routes.LimitDateController.onPageLoad(departureId, mode).url
  private val date                = LocalDate.now

  private val customsOffice = CustomsOffice("AB123", "Dundee", None)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficeService))

  "LimitDate Controller" - {

    "must return OK and the correct view for a GET when given a customs office" in {

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(customsOffice))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LimitDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode, departureId, maxDateArg, customsOffice.toString)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(customsOffice))

      val userAnswers = emptyUserAnswers.setValue(LimitDatePage, date)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      val filledForm = form.bind(
        Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )
      )

      val view = injector.instanceOf[LimitDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, mode, departureId, maxDateArg, customsOffice.toString)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.set(any())) `thenReturn` Future.successful(true)

      val request = FakeRequest(POST, limitDateRoute)
        .withFormUrlEncodedBody(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(customsOffice))

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, limitDateRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[LimitDateView]

      contentAsString(result) mustEqual
        view(filledForm, mode, departureId, maxDateArg, customsOffice.toString)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, limitDateRoute)
        .withFormUrlEncodedBody(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
