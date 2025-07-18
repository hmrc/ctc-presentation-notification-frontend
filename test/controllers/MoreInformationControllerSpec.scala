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
import pages.behaviours.PageBehaviours
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.MoreInformationView

class MoreInformationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with PageBehaviours {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "MoreInformation Controller" - {
    "return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.MoreInformationController.onPageLoad(departureId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[MoreInformationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(lrn.value, departureId)(request, messages).toString
    }

    "redirect successfully when calling onSubmit" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, routes.MoreInformationController.onSubmit(departureId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }
  }
}
