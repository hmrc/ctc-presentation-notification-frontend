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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import generators.Generators
import models.Index
import models.reference.TransportMode.InlandMode
import models.reference.transport.border.active.Identification
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.transport.InlandModePage
import services.CheckYourAnswersReferenceDataService
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import viewModels.transport.border.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PresentationNotificationAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val mockCyaService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]
  private val mockFrontendAppConfig: FrontendAppConfig             = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    reset(mockCyaService)
    super.beforeEach()
  }

  "PresentationNotificationAnswersViewModel" - {

    "must return the view model" in {

      val userAnswers = emptyUserAnswers
        .setValue(pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage(Index(0)), "foo1")
        .setValue(pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage(Index(1)), "foo2")

      val country = Country(CountryCode("a"), "b")
      when(mockCyaService.getCountry(any())(any()))
        .thenReturn(Future.successful(country))

      val activeBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModelProvider()

      val viewModelProvider = new PresentationNotificationAnswersViewModelProvider(mockFrontendAppConfig, activeBorderAnswersViewModelProvider, mockCyaService)

      val result = viewModelProvider.apply(userAnswers, departureId).futureValue

      result.sections.length mustEqual 11
      result.sections(9).sectionTitle.value mustEqual "Border means of transport"
    }

    "must return the view model when InlandMode is mail" in {

      val userAnswers = emptyUserAnswers
        .setValue(InlandModePage, InlandMode("5", "desc"))
        .setValue(pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage(Index(0)), "foo1")
        .setValue(pages.transport.departureTransportMeans.TransportMeansIdentificationNumberPage(Index(1)), "foo2")

      val country = Country(CountryCode("a"), "b")
      when(mockCyaService.getCountry(any())(any()))
        .thenReturn(Future.successful(country))

      val activeBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModelProvider()

      val viewModelProvider = new PresentationNotificationAnswersViewModelProvider(mockFrontendAppConfig, activeBorderAnswersViewModelProvider, mockCyaService)

      val result = viewModelProvider.apply(userAnswers, departureId).futureValue

      result.sections.length mustEqual 9
      result.sections(7).sectionTitle.value mustEqual "Border means of transport"
    }

    "must return the view model when ActiveBorderTransportMeans is defined" in {

      val userAnswers = emptyUserAnswers
        .setValue(pages.transport.border.active.IdentificationPage(Index(0)), Identification("foo", "bar"))

      val country = Country(CountryCode("a"), "b")
      when(mockCyaService.getCountry(any())(any()))
        .thenReturn(Future.successful(country))

      val activeBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModelProvider()

      val viewModelProvider = new PresentationNotificationAnswersViewModelProvider(mockFrontendAppConfig, activeBorderAnswersViewModelProvider, mockCyaService)

      val result = viewModelProvider.apply(userAnswers, departureId).futureValue

      result.sections.length mustEqual 9
      result.sections(7).sectionTitle.value mustEqual "Border means of transport 1"
    }
  }
}
