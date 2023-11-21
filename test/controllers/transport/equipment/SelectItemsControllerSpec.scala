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
import forms.SelectableFormProvider
import generators.Generators
import models.{Index, NormalMode}
import pages.transport.equipment.ItemPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.transport.equipment.SelectItemsViewModel
import views.html.transport.equipment.SelectItemsView

class SelectItemsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val viewModel: SelectItemsViewModel = SelectItemsViewModel(emptyUserAnswers)

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("transport.equipment.selectItems", viewModel.items)
  private val mode         = NormalMode

  private lazy val controllerRoute = controllers.transport.equipment.routes.SelectItemsController.onPageLoad(departureId, mode, Index(0), Index(0)).url

  "SelectItemsController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[SelectItemsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, Index(0), Index(0), departureId, viewModel, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(ItemPage(equipmentIndex, Index(0)), viewModel.items.values.head)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> viewModel.items.values.head.value))

      val view = injector.instanceOf[SelectItemsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, Index(0), Index(0), departureId, viewModel, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, controllerRoute)
        .withFormUrlEncodedBody(("value", viewModel.items.values.head.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, controllerRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[SelectItemsView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, Index(0), Index(0), departureId, viewModel, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, controllerRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, controllerRoute)
        .withFormUrlEncodedBody(("value", viewModel.items.values.head.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
