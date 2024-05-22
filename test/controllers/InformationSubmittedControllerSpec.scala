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
import generated._
import generators.Generators
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CustomsOfficesService, DepartureMessageService}
import views.html.InformationSubmittedView

import scala.concurrent.Future

class InformationSubmittedControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val declarationSubmittedRoute: String =
    routes.InformationSubmittedController.onPageLoad(departureId).url

  private val mockDepartureMessageService: DepartureMessageService = mock[DepartureMessageService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind(classOf[DepartureMessageService]).toInstance(mockDepartureMessageService),
        bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficeService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockCustomsOfficeService)
  }

  "InformationSubmittedController" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC170CType], nonEmptyString, arbitrary[CustomsOffice]) {
        (cc170cType, customsOfficeId, customsOffice) =>
          beforeEach()

          val transitOperation = cc170cType.TransitOperation.copy(LRN = lrn.value)
          val ie170 = cc170cType.copy(
            TransitOperation = transitOperation,
            CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(customsOfficeId)
          )

          when(mockDepartureMessageService.getIE170(any())(any(), any()))
            .thenReturn(Future.successful(Some(ie170)))

          when(mockCustomsOfficeService.getCustomsOfficeById(any())(any()))
            .thenReturn(Future.successful(customsOffice.copy(id = customsOfficeId)))

          when(mockSessionRepository.remove(any()))
            .thenReturn(Future.successful(true))

          val request = FakeRequest(GET, declarationSubmittedRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[InformationSubmittedView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(lrn.value, customsOffice)(request, messages).toString

          verify(mockDepartureMessageService).getIE170(eqTo(departureId))(any(), any())
          verify(mockCustomsOfficeService).getCustomsOfficeById(eqTo(customsOfficeId))(any())
          verify(mockSessionRepository).remove(eqTo(departureId))
      }
    }

    "must redirect to tech difficulties" - {
      "when IE170 not found" in {
        when(mockDepartureMessageService.getIE170(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, declarationSubmittedRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
