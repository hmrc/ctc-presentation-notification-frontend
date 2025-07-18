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

import controllers.actions.*
import models.UserAnswers
import navigation.*
import navigation.BorderGroupNavigator.BorderGroupNavigatorProvider
import navigation.DepartureTransportMeansGroupNavigator.DepartureTransportMeansGroupNavigatorProvider
import navigation.EquipmentGroupNavigator.EquipmentGroupNavigatorProvider
import navigation.GoodsReferenceGroupNavigator.GoodsReferenceGroupNavigatorProvider
import navigation.SealGroupNavigator.SealGroupNavigatorProvider
import navigator.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.cache.AsyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService, NationalitiesService}

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite & SpecBase =>

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
    when(mockDataRetrievalActionProvider.apply(any())) `thenReturn` new FakeDataRetrievalAction(userAnswers)

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator                                                       = new FakeNavigator(onwardRoute)
  protected val fakeLoadingNavigator: LoadingNavigator                                         = new FakeLoadingNavigator(onwardRoute)
  protected val fakeLocationOfGoodsNavigator: LocationOfGoodsNavigator                         = new FakeLocationOfGoodsNavigator(onwardRoute)
  protected val fakeBorderGroupNavigatorProvider: BorderGroupNavigatorProvider                 = new FakeBorderGroupNavigatorProvider(onwardRoute)
  protected val fakeBorderNavigator: BorderNavigator                                           = new FakeBorderNavigator(onwardRoute)
  protected val fakeContainerNavigator: ContainerNavigator                                     = new FakeContainerNavigator(onwardRoute)
  protected val fakeEquipmentGroupNavigatorProvider: EquipmentGroupNavigatorProvider           = new FakeEquipmentGroupNavigatorProvider(onwardRoute)
  protected val fakeEquipmentNavigator: EquipmentNavigator                                     = new FakeEquipmentNavigator(onwardRoute)
  protected val fakeSealGroupNavigatorProvider: SealGroupNavigatorProvider                     = new FakeSealGroupNavigatorProvider(onwardRoute)
  protected val fakeSealNavigator: SealNavigator                                               = new FakeSealNavigator(onwardRoute)
  protected val fakeGoodsReferenceGroupNavigatorProvider: GoodsReferenceGroupNavigatorProvider = new FakeGoodsReferenceGroupNavigatorProvider(onwardRoute)
  protected val fakeGoodsReferenceNavigator: GoodsReferenceNavigator                           = new FakeGoodsReferenceNavigator(onwardRoute)

  protected val fakeDepartureTransportMeansGroupNavigatorProvider: DepartureTransportMeansGroupNavigatorProvider =
    new FakeDepartureTransportMeansGroupNavigatorProvider(onwardRoute)

  protected val fakeDepartureTransportMeansNavigator: DepartureTransportMeansNavigator = new FakeDepartureTransportMeansNavigator(onwardRoute)
  protected val fakeRepresentativeNavigator: RepresentativeNavigator                   = new FakeRepresentativeNavigator(onwardRoute)

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[AsyncCacheApi].to[FakeAsyncCacheApi],
        bind[Navigator].toInstance(fakeNavigator),
        bind[LoadingNavigator].toInstance(fakeLoadingNavigator),
        bind[LocationOfGoodsNavigator].toInstance(fakeLocationOfGoodsNavigator),
        bind[BorderGroupNavigatorProvider].toInstance(fakeBorderGroupNavigatorProvider),
        bind[BorderNavigator].toInstance(fakeBorderNavigator),
        bind[ContainerNavigator].toInstance(fakeContainerNavigator),
        bind[EquipmentGroupNavigatorProvider].toInstance(fakeEquipmentGroupNavigatorProvider),
        bind[EquipmentNavigator].toInstance(fakeEquipmentNavigator),
        bind[SealGroupNavigatorProvider].toInstance(fakeSealGroupNavigatorProvider),
        bind[SealNavigator].toInstance(fakeSealNavigator),
        bind[GoodsReferenceGroupNavigatorProvider].toInstance(fakeGoodsReferenceGroupNavigatorProvider),
        bind[GoodsReferenceNavigator].toInstance(fakeGoodsReferenceNavigator),
        bind[RepresentativeNavigator].toInstance(fakeRepresentativeNavigator),
        bind[DepartureTransportMeansGroupNavigatorProvider].toInstance(fakeDepartureTransportMeansGroupNavigatorProvider),
        bind[DepartureTransportMeansNavigator].toInstance(fakeDepartureTransportMeansNavigator)
      )

  protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

  def phase5App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> false)

  def phase6App: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> true)
}
