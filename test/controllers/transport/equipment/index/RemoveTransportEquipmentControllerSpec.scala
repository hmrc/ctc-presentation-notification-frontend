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
import controllers.routes
import controllers.transport.equipment.index.{routes => equipmentRoutes}
import forms.YesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.sections.transport.equipment.EquipmentSection
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.equipment.index.RemoveTransportEquipmentView

import scala.concurrent.Future

class RemoveTransportEquipmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                       = new YesNoFormProvider()
  private val form                               = formProvider("transport.equipment.index.removeTransportEquipment", equipmentIndex.display)
  private val mode                               = NormalMode
  private lazy val removeTransportEquipmentRoute = equipmentRoutes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, equipmentIndex).url

  "RemoveTransportEquipment Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(emptyUserAnswers.setValue(EquipmentSection(equipmentIndex), Json.obj()))

      val request = FakeRequest(GET, removeTransportEquipmentRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveTransportEquipmentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode, equipmentIndex)(request, messages).toString

    }

    "must redirect to the next page" - {
      "when yes is submitted" in {
        setExistingUserAnswers(emptyUserAnswers.setValue(EquipmentSection(equipmentIndex), Json.obj()))

        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, removeTransportEquipmentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(EquipmentSection(equipmentIndex)) mustNot be(defined)

      }

      "when no is submitted" in {
        setExistingUserAnswers(emptyUserAnswers.setValue(EquipmentSection(equipmentIndex), Json.obj()))

        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, removeTransportEquipmentRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(EquipmentSection(equipmentIndex)) must be(defined)

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(emptyUserAnswers.setValue(EquipmentSection(equipmentIndex), Json.obj()))

      val request   = FakeRequest(POST, removeTransportEquipmentRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveTransportEquipmentView]

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode, equipmentIndex)(request, messages).toString

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeTransportEquipmentRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeTransportEquipmentRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
