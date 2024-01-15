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

package base

import controllers.actions._
import models.UserAnswers
import navigation._
import navigator._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService, NationalitiesService}

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite with SpecBase =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository); reset(mockDataRetrievalActionProvider)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  final val mockSessionRepository: SessionRepository                     = mock[SessionRepository]
  final val mockDataRetrievalActionProvider: DataRetrievalActionProvider = mock[DataRetrievalActionProvider]
  final val mockCountriesService: CountriesService                       = mock[CountriesService]
  final val mockCustomsOfficeService: CustomsOfficesService              = mock[CustomsOfficesService]
  final val mockNationalitiesService: NationalitiesService               = mock[NationalitiesService]

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  protected def setExistingUserAnswers(userAnswers: UserAnswers): Unit = setUserAnswers(Some(userAnswers))

  protected def setNoExistingUserAnswers(): Unit = setUserAnswers(None)

  private def setUserAnswers(userAnswers: Option[UserAnswers]): Unit =
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(userAnswers)

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator                                               = new FakeNavigator(onwardRoute)
  protected val fakeLoadingNavigator: LoadingNavigator                                 = new FakeLoadingNavigator(onwardRoute)
  protected val fakeLocationOfGoodsNavigator: LocationOfGoodsNavigator                 = new FakeLocationOfGoodsNavigator(onwardRoute)
  protected val fakeBorderNavigatorProvider: BorderNavigator                           = new FakeBorderNavigator(onwardRoute)
  protected val fakeContainerNavigator: ContainerNavigator                             = new FakeContainerNavigator(onwardRoute)
  protected val fakeEquipmentNavigator: EquipmentNavigator                             = new FakeEquipmentNavigator(onwardRoute)
  protected val fakeDepartureTransportMeansNavigator: DepartureTransportMeansNavigator = new FakeDepartureTransportMeansNavigator(onwardRoute)

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[Navigator].toInstance(fakeNavigator),
        bind[LoadingNavigator].toInstance(fakeLoadingNavigator),
        bind[LocationOfGoodsNavigator].toInstance(fakeLocationOfGoodsNavigator),
        bind[BorderNavigator].toInstance(fakeBorderNavigatorProvider),
        bind[ContainerNavigator].toInstance(fakeContainerNavigator),
        bind[EquipmentNavigator].toInstance(fakeEquipmentNavigator),
        bind[DepartureTransportMeansNavigator].toInstance(fakeDepartureTransportMeansNavigator)
      )

  protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()
}
