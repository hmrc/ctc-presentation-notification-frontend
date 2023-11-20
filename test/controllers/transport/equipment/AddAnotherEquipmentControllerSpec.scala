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

import base.TestMessageData.messageData
import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import controllers.transport.equipment.{routes => euipmentRoutes}
import controllers.transport.equipment.index.{routes => indexRoutes}
import controllers.transport.equipment.index.seals.{routes => sealRoutes}
import forms.AddAnotherFormProvider
import generators.Generators
import models.messages.Authorisation
import models.messages.AuthorisationType.{C521, C523}
import models.{Index, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.ListItem
import viewModels.transport.equipment.AddAnotherEquipmentViewModel
import viewModels.transport.equipment.AddAnotherEquipmentViewModel.AddAnotherEquipmentViewModelProvider
import views.html.transport.equipment.AddAnotherEquipmentView

class AddAnotherEquipmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherEquipmentViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore)

  private val mode = NormalMode

  private lazy val addAnotherEquipmentRoute = euipmentRoutes.AddAnotherEquipmentController.onPageLoad(departureId, mode).url

  private val mockViewModelProvider = mock[AddAnotherEquipmentViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherEquipmentViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxEquipmentNumbers - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxEquipmentNumbers)(listItem)

  private val viewModel = arbitrary[AddAnotherEquipmentViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherEquipment Controller" - {

    "when 0 equipment" - {
      "must redirect to add equipment yes/no page" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherEquipmentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          euipmentRoutes.AddTransportEquipmentYesNoController.onPageLoad(departureId, mode).url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherEquipmentRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherEquipmentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), departureId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherEquipmentRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherEquipmentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), departureId, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to Add Container Identification Number page for the next index" in {
          val oneListItem          = Seq(listItem)
          val notMaxedOutViewModel = viewModel.copy(listItems = oneListItem)

          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, true)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "containerId")

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, addAnotherEquipmentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual indexRoutes.AddContainerIdentificationNumberYesNoController.onPageLoad(departureId, mode, Index(1)).url
        }

        "must redirect to Seal Identification Number page for the next index" in {
          val notMaxedOutViewModel = viewModel.copy(listItems = Nil)

          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          val authData = Some(
            Seq(
              Authorisation(C521, "AB123"),
              Authorisation(C523, "CD456")
            )
          )

          val userAnswers = emptyUserAnswers
            .copy(departureData = messageData.copy(Authorisation = authData))
            .setValue(ContainerIndicatorPage, false)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, addAnotherEquipmentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual sealRoutes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, sealIndex).url
        }

        "must redirect to Add Seal page for the next index" in {
          val notMaxedOutViewModel = viewModel.copy(listItems = Nil)

          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, false)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, addAnotherEquipmentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual indexRoutes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex).url
        }
      }

      "when no submitted" - {
        "must redirect to next page" in {
          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherEquipmentRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual "#" //TODO to be updated when CYA built
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherEquipmentRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "#" //TODO to be updated when CYA built
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherEquipmentRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherEquipmentView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, departureId, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherEquipmentRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherEquipmentRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
