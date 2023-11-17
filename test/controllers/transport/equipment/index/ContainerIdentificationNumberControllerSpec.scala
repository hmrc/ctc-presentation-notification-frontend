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

package controllers.transport.equipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.transport.equipment.ContainerIdentificationNumberFormProvider
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.equipment.index.ContainerIdentificationNumberView

import java.util.UUID
import scala.concurrent.Future

class ContainerIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                            = new ContainerIdentificationNumberFormProvider()
  private def form(otherIds: Seq[String] = Nil)       = formProvider("transport.equipment.index.containerIdentificationNumber", otherIds)
  private val validAnswer                             = "testString"
  private val mode                                    = NormalMode
  private def identificationNumberRoute(index: Index) = routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, index).url

  "IdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, identificationNumberRoute(equipmentIndex))

      val result = route(app, request).value

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form(), departureId, mode, equipmentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationNumberRoute(equipmentIndex))

      val result = route(app, request).value

      val filledForm = form().bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode, equipmentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
