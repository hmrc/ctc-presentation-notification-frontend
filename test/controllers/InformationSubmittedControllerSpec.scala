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
import generators.Generators
import models.UserAnswers
import models.reference.CustomsOffice
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CustomsOfficesService
import views.html.InformationSubmittedView

import scala.concurrent.Future

class InformationSubmittedControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val declarationSubmittedRoute: String = routes.InformationSubmittedController.onPageLoad("651431d7e3b05b21").url

  private val customsOffice = CustomsOffice("AB123", "BIRMINGHAM CONTAINERBASE", Some("+44 (0)121 345 6789"))

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficeService))

  "InformationSubmittedController" - {

    "must return OK and the correct view for a GET and purge the cache" in {

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(customsOffice))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, declarationSubmittedRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[InformationSubmittedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view("ABCD1234567890123", customsOffice)(request, messages).toString

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())
      userAnswersCaptor.getValue.data mustBe emptyUserAnswers.data
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, declarationSubmittedRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
