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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import viewModels.{PresentationNotificationAnswersViewModel, Section}
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with PageBehaviours {

  private lazy val mockViewModelProvider = mock[PresentationNotificationAnswersViewModelProvider]
  val sampleSections: Seq[Section]       = arbitrary[List[Section]].sample.value

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[PresentationNotificationAnswersViewModelProvider].toInstance(mockViewModelProvider))

  "CheckYourAnswersController" - {
    "return OK and the correct view for a GET" in {

      when(mockViewModelProvider.apply(any(), any(), any())(any()))
        .thenReturn(PresentationNotificationAnswersViewModel(sampleSections))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(departureId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[CheckYourAnswersView]

      println(result)

      status(result) mustBe OK

      contentAsString(result) mustEqual view(lrn.value, departureId, sampleSections)(request, messages).toString
    }

    "redirect successfully when calling onSubmit" ignore { //todo not implemented yet - will be confirmation page

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(departureId).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }
  }
}
