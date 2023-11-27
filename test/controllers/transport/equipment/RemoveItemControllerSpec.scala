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

package controllers.transport.equipment

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import controllers.transport.equipment.{routes => equipmentRoutes}
import forms.YesNoFormProvider
import models.reference.Item
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.sections.transport.equipment.ItemSection
import pages.transport.equipment.ItemPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.equipment.RemoveItemView

import scala.concurrent.Future

class RemoveItemControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider         = new YesNoFormProvider()
  private val form                 = formProvider("transport.equipment.removeItem", equipmentIndex.display)
  private val mode                 = NormalMode
  private lazy val removeItemRoute = equipmentRoutes.RemoveItemController.onPageLoad(departureId, mode, equipmentIndex, itemIndex).url

  private val item: Item = Item(1234, "desc")

  "RemoveItem Controller" - {

    "must return OK and the correct view for a GET" in {
      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(ItemSection(equipmentIndex, itemIndex), Json.obj())
          .setValue(ItemPage(equipmentIndex, itemIndex), item)
      )

      val request = FakeRequest(GET, removeItemRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveItemView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, departureId, mode, equipmentIndex, itemIndex, item)(request, messages).toString

    }

    "must redirect to the next page" - {
      "when yes is submitted" in {
        setExistingUserAnswers(
          emptyUserAnswers
            .setValue(ItemSection(equipmentIndex, itemIndex), Json.obj())
            .setValue(ItemPage(equipmentIndex, itemIndex), item)
        )

        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, removeItemRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(equipmentIndex, itemIndex)) mustNot be(defined)

      }

      "when no is submitted" in {
        setExistingUserAnswers(
          emptyUserAnswers
            .setValue(ItemSection(equipmentIndex, itemIndex), Json.obj())
            .setValue(ItemPage(equipmentIndex, itemIndex), item)
        )

        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, removeItemRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())
        userAnswersCaptor.getValue.get(ItemSection(equipmentIndex, itemIndex)) must be(defined)

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(ItemSection(equipmentIndex, itemIndex), Json.obj())
          .setValue(ItemPage(equipmentIndex, itemIndex), item)
      )

      val request   = FakeRequest(POST, removeItemRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveItemView]

      contentAsString(result) mustEqual
        view(boundForm, departureId, mode, equipmentIndex, itemIndex, item)(request, messages).toString

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeItemRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeItemRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
