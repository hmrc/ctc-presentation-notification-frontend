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

import base.{AppWithDefaultMockFixtures, SpecBase, TestMessageData}
import matchers.JsonMatchers
import models.LocationOfGoodsIdentification
import models.messages.MessageData
import models.reference.BorderMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{LocationOfGoodsIdentificationTypeService, LocationTypeService, TransportModeCodesService}
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import viewModels.{PresentationNotificationAnswersViewModel, Section}
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with PageBehaviours {

  val border1: BorderMode = BorderMode("1", "A2GjAWj")
  val border2: BorderMode = BorderMode("2", "jn58227")

  private val borderModes: Seq[BorderMode] = Seq(border1, border2)
  private val identifications              = Seq(LocationOfGoodsIdentification("qualifier1", "description1"), LocationOfGoodsIdentification("qualifier2", "description2"))

  private lazy val mockViewModelProvider = mock[PresentationNotificationAnswersViewModelProvider]
  val sampleSections: Seq[Section]       = arbitrary[List[Section]].sample.value

  private val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]
  private val mockLocationTypeService                                  = mock[LocationTypeService]
  private val mockIdentificationService                                = mock[LocationOfGoodsIdentificationTypeService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[PresentationNotificationAnswersViewModelProvider].toInstance(mockViewModelProvider))
      .overrides(bind(classOf[TransportModeCodesService]).toInstance(mockTransportModeCodesService))
      .overrides(bind(classOf[LocationTypeService]).toInstance(mockLocationTypeService))
      .overrides(bind(classOf[LocationOfGoodsIdentificationTypeService]).toInstance(mockIdentificationService))

  "CheckYourAnswersController" - {
    "return OK and the correct view for a GET" in {

      when(mockTransportModeCodesService.getBorderModes()(any())).thenReturn(Future.successful(borderModes))
      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(locationTypes))
      when(mockIdentificationService.getLocationOfGoodsIdentificationTypes(any())(any())).thenReturn(Future.successful(identifications))
      when(mockViewModelProvider.apply(any(), any(), any(), any(), any())(any()))
        .thenReturn(PresentationNotificationAnswersViewModel(sampleSections))

      setExistingUserAnswers(emptyUserAnswers.copy(departureData = TestMessageData.messageData))

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(departureId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[CheckYourAnswersView]

      status(result) mustBe OK

      contentAsString(result) mustEqual view(lrn.value, departureId, sampleSections)(request, messages).toString
      verify(mockLocationTypeService, times(1)).getLocationTypes(any())(any())
      verify(mockIdentificationService, times(1)).getLocationOfGoodsIdentificationTypes(any())(any())
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
