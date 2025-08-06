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
import generated.TransportEquipmentType03
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.transport.equipment.{ItemsSection, SealsSection}
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.{AddContainerIdentificationNumberYesNoPage, ContainerIdentificationNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.Future

class TransportEquipmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[TransportEquipmentTransformer]

  private lazy val mockSealsTransformer           = mock[SealsTransformer]
  private lazy val mockGoodsReferencesTransformer = mock[GoodsReferencesTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[SealsTransformer].toInstance(mockSealsTransformer),
        bind[GoodsReferencesTransformer].toInstance(mockGoodsReferencesTransformer)
      )

  "must transform data" - {
    "when optional values are all defined" in {
      forAll(listWithMaxLength[TransportEquipmentType03]().map(_.map(_.copy(containerIdentificationNumber = Some("value"))))) {
        transportEquipments =>
          transportEquipments.zipWithIndex.map {
            case (_, i) =>
              val equipmentIndex = Index(i)

              when(mockSealsTransformer.transform(any(), eqTo(equipmentIndex)))
                .thenReturn {
                  ua => Future.successful(ua.setValue(SealsSection(equipmentIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
                }

              when(mockGoodsReferencesTransformer.transform(any(), eqTo(equipmentIndex)))
                .thenReturn {
                  ua => Future.successful(ua.setValue(ItemsSection(equipmentIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
                }
          }

          val result = transformer.transform(transportEquipments).apply(emptyUserAnswers).futureValue
          result.get(AddTransportEquipmentYesNoPage).value mustEqual true

          transportEquipments.zipWithIndex.map {
            case (te, i) =>
              val equipmentIndex = Index(i)
              result.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)).value mustEqual true
              result.get(ContainerIdentificationNumberPage(equipmentIndex)) mustEqual te.containerIdentificationNumber
              result.getValue(SealsSection(equipmentIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
              result.getValue(ItemsSection(equipmentIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
          }
      }
    }

    "when optional values are undefined" in {
      forAll(listWithMaxLength[TransportEquipmentType03]().map(_.map(_.copy(containerIdentificationNumber = None)))) {
        transportEquipments =>
          transportEquipments.zipWithIndex.map {
            case (_, i) =>
              val equipmentIndex = Index(i)

              when(mockSealsTransformer.transform(any(), eqTo(equipmentIndex)))
                .thenReturn {
                  ua => Future.successful(ua.setValue(SealsSection(equipmentIndex), JsArray()))
                }

              when(mockGoodsReferencesTransformer.transform(any(), eqTo(equipmentIndex)))
                .thenReturn {
                  ua => Future.successful(ua.setValue(ItemsSection(equipmentIndex), JsArray()))
                }
          }

          val result = transformer.transform(transportEquipments).apply(emptyUserAnswers).futureValue
          result.get(AddTransportEquipmentYesNoPage).value mustEqual true

          transportEquipments.zipWithIndex.map {
            case (te, i) =>
              val equipmentIndex = Index(i)

              result.get(AddContainerIdentificationNumberYesNoPage(equipmentIndex)).value mustEqual false
              result.get(ContainerIdentificationNumberPage(equipmentIndex)) must not be defined
              result.getValue(SealsSection(equipmentIndex)) mustEqual JsArray()
              result.getValue(ItemsSection(equipmentIndex)) mustEqual JsArray()
          }
      }
    }

    "when transportEquipment is undefined" in {
      val result = transformer.transform(Nil).apply(emptyUserAnswers).futureValue
      result.get(AddTransportEquipmentYesNoPage).value mustEqual false
    }
  }
}
