/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformer

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.ConsignmentType23
import generators.Generators
import models.reference.TransportMode.InlandMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.transport.departureTransportMeans.TransportMeansListSection
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.InlandModePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import services.TransportModeCodesService

import scala.concurrent.Future

class ConsignmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[ConsignmentTransformer]

  private lazy val mockTransportEquipmentTransformer      = mock[TransportEquipmentTransformer]
  private lazy val mockDepartureTransportMeansTransformer = mock[DepartureTransportMeansTransformer]

  private lazy val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[TransportEquipmentTransformer].toInstance(mockTransportEquipmentTransformer),
        bind[DepartureTransportMeansTransformer].toInstance(mockDepartureTransportMeansTransformer),
        bind[TransportModeCodesService].toInstance(mockTransportModeCodesService)
      )

  "must transform data" - {
    "when options defined" in {
      val inlandMode = InlandMode("1", "mode")

      forAll(arbitrary[ConsignmentType23].map(_.copy(inlandModeOfTransport = Some(inlandMode.code)))) {
        consignment =>
          when(mockTransportEquipmentTransformer.transform(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(EquipmentsSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockDepartureTransportMeansTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(TransportMeansListSection, JsArray(Seq(Json.obj("foo" -> "bar")))))
            }

          when(mockTransportModeCodesService.getInlandMode(any())(any()))
            .thenReturn(Future.successful(inlandMode))

          val result = transformer.transform(consignment).apply(emptyUserAnswers).futureValue

          result.getValue(EquipmentsSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(TransportMeansListSection) mustEqual JsArray(Seq(Json.obj("foo" -> "bar")))
          result.getValue(InlandModePage) mustEqual inlandMode
      }
    }
  }
}
