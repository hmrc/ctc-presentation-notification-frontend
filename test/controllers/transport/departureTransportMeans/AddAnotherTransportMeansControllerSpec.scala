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

package controllers.transport.departureTransportMeans

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import controllers.transport.departureTransportMeans.{routes => departureTransportMeansRoutes}
import forms.AddAnotherFormProvider
import generators.Generators
import models.CheckMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.ListItem
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel.AddAnotherTransportMeansViewModelProvider
import views.html.transport.departureTransportMeans.AddAnotherTransportMeansView

class AddAnotherTransportMeansControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherTransportMeansViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore)

  private lazy val addAnotherTransportMeansRouteCheckMode =
    departureTransportMeansRoutes.AddAnotherTransportMeansController.onPageLoad(departureId, CheckMode).url

  private val mockViewModelProvider = mock[AddAnotherTransportMeansViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherTransportMeansViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxTransportMeans - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxTransportMeans)(listItem)

  private val viewModel = arbitrary[AddAnotherTransportMeansViewModel].sample.value

  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherTransportMeans Controller" - {

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {

        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherTransportMeansRouteCheckMode)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherTransportMeansView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {

        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherTransportMeansRouteCheckMode)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherTransportMeansView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherTransportMeansRouteCheckMode)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherTransportMeansView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherTransportMeansRouteCheckMode)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherTransportMeansRouteCheckMode)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
