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
import models.AuditType.PresentationNotification
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.submission.{AuditService, SubmissionService}
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import viewModels.{PresentationNotificationAnswersViewModel, Section}
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with PageBehaviours {

  private lazy val mockViewModelProvider = mock[PresentationNotificationAnswersViewModelProvider]
  private lazy val mockSubmissionService = mock[SubmissionService]
  private lazy val mockAuditService      = mock[AuditService]

  val sampleSections: Seq[Section] = arbitrary[List[Section]].sample.value

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[PresentationNotificationAnswersViewModelProvider].toInstance(mockViewModelProvider),
        bind[SubmissionService].toInstance(mockSubmissionService),
        bind[AuditService].toInstance(mockAuditService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    reset(mockSubmissionService)
    reset(mockAuditService)
  }

  "CheckYourAnswersController" - {
    "return OK and the correct view for a GET" in {

      when(mockViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(PresentationNotificationAnswersViewModel(sampleSections)))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(departureId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[CheckYourAnswersView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(lrn.value, departureId, sampleSections)(request, messages).toString
    }

    "redirect to confirmation page when submission successful" in {

      when(mockSubmissionService.submit(any(), any())(any())).thenReturn(response(OK))

      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(departureId).url)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result).value mustEqual routes.InformationSubmittedController.onPageLoad(departureId).url

      verify(mockSubmissionService).submit(eqTo(userAnswers), eqTo(departureId))(any())
      verify(mockAuditService).audit(eqTo(PresentationNotification), eqTo(userAnswers))(any())
    }

    "redirect to technical difficulties page when submission unsuccessful" in {

      forAll(Gen.choose(400, 599)) {
        errorCode =>
          beforeEach()

          when(mockSubmissionService.submit(any(), any())(any())).thenReturn(response(errorCode))

          val userAnswers = emptyUserAnswers
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(departureId).url)

          val result = route(app, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustEqual routes.ErrorController.technicalDifficulties().url

          verify(mockSubmissionService).submit(eqTo(userAnswers), eqTo(departureId))(any())
          verifyNoInteractions(mockAuditService)
      }
    }
  }
}
