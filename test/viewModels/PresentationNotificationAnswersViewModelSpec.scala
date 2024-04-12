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

import base.SpecBase
import config.FrontendAppConfig
import connectors.ReferenceDataConnector
import generators.Generators
import models.messages.Consignment
import models.reference.TransportMode.InlandMode
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.InlandModePage
import services.CheckYourAnswersReferenceDataService
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import viewModels.transport.border.active.ActiveBorderAnswersViewModel
import viewModels.transport.border.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PresentationNotificationAnswersViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  //mock reference too
  private val mockRefDataConnector: ReferenceDataConnector     = mock[ReferenceDataConnector]
  private val cyaService: CheckYourAnswersReferenceDataService = new CheckYourAnswersReferenceDataService(mockRefDataConnector)
  private val config: FrontendAppConfig                        = injector.instanceOf[FrontendAppConfig]

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "PresentationNotificationAnswersViewModelSpec" - {

    "must return the view model" in {

      val consignment: Consignment = emptyUserAnswers.departureData.Consignment.copy(ActiveBorderTransportMeans = None)
      val dep                      = emptyUserAnswers.departureData.copy(Consignment = consignment)
      val userAnswers              = emptyUserAnswers.copy(departureData = dep)

      val country = Country(CountryCode("a"), "b")
      when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
        .thenReturn(Future.successful(country))

      val activeBorderAnswersViewModelProvider: ActiveBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider
      val viewModelProvider                                                          = new PresentationNotificationAnswersViewModelProvider()(config, activeBorderAnswersViewModelProvider, cyaService)

      val section: Future[PresentationNotificationAnswersViewModel] = viewModelProvider.apply(userAnswers, departureId)

      whenReady(section) {
        viewModel =>
          viewModel.sections(5).rows.length mustBe 0
          viewModel.sections(6).rows.length mustBe 1
          viewModel.sections(7).rows.length mustBe 0
          viewModel.sections.length mustBe 9

      }

    }

    "must return the view model when InlandMode is mail" in {

      val consignment: Consignment = emptyUserAnswers.departureData.Consignment.copy(ActiveBorderTransportMeans = None)
      val dep                      = emptyUserAnswers.departureData.copy(Consignment = consignment)
      val userAnswers              = emptyUserAnswers.copy(departureData = dep).setValue(InlandModePage, InlandMode("5", "desc"))

      val country = Country(CountryCode("a"), "b")
      when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
        .thenReturn(Future.successful(country))

      val activeBorderAnswersViewModelProvider: ActiveBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider
      val viewModelProvider                                                          = new PresentationNotificationAnswersViewModelProvider()(config, activeBorderAnswersViewModelProvider, cyaService)

      val section = viewModelProvider.apply(userAnswers, departureId)

      whenReady(section) {
        viewModel =>
          viewModel.sections(5).rows.length mustBe 1
          viewModel.sections(6).rows.length mustBe 1
          viewModel.sections(7).rows.length mustBe 0
          viewModel.sections.length mustBe 9

      }

    }

    "must return the view model when ActiveBorderTransportMeans is defined" in {

      forAll(arbitraryActiveBorderTransportMeans.arbitrary) {
        arbitraryActiveBorderTransportMeans =>
          val consignment: Consignment = emptyUserAnswers.departureData.Consignment.copy(ActiveBorderTransportMeans = arbitraryActiveBorderTransportMeans)
          val dep                      = emptyUserAnswers.departureData.copy(Consignment = consignment)
          val userAnswers              = emptyUserAnswers.copy(departureData = dep)

          val country = Country(CountryCode("a"), "b")
          when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
            .thenReturn(Future.successful(country))

          val activeBorderAnswersViewModelProvider: ActiveBorderAnswersViewModelProvider = new ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider
          val viewModelProvider                                                          = new PresentationNotificationAnswersViewModelProvider()(config, activeBorderAnswersViewModelProvider, cyaService)

          val section = viewModelProvider.apply(userAnswers, departureId)

          whenReady(section) {
            viewModel =>
              viewModel.sections(5).rows.length mustBe 0
              viewModel.sections(6).rows.length mustBe 1
              viewModel.sections(7).rows.length mustBe 0
              viewModel.sections.length mustBe 9

          }

      }
    }

  }
}
