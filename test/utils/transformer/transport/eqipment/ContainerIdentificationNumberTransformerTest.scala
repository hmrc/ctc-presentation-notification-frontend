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

package utils.transformer.transport.eqipment

import base.SpecBase
import models.messages.TransportEquipment
import models.{Index, UserAnswers}
import pages.transport.border.active.IdentificationNumberPage
import pages.transport.equipment.AddAnotherTransportEquipmentPage
import pages.transport.equipment.index.ContainerIdentificationNumberPage

class ContainerIdentificationNumberTransformerTest extends SpecBase {

  val transformer = new ContainerIdentificationNumberTransformer()

  "ContainerIdentificationNumberTransformer" - {
    "must return updated answers with ContainerIdentificationNumberPage if container id exist for the equipment" in {
      val userAnswersWithEquipments = UserAnswers.setTransportEquipmentLens
        .set(
          Option(
            List(
              TransportEquipment("1", Some("container id 1"), 0, None, None),
              TransportEquipment("2", None, 0, None, None),
              TransportEquipment("3", Some("container id 3"), 0, None, None)
            )
          )
        )(emptyUserAnswers)
        .set(AddAnotherTransportEquipmentPage(Index(0)), true)
        .get
        .set(AddAnotherTransportEquipmentPage(Index(1)), true)
        .get
        .set(AddAnotherTransportEquipmentPage(Index(2)), true)
        .get

      userAnswersWithEquipments.get(IdentificationNumberPage(Index(0))) mustBe None
      userAnswersWithEquipments.get(IdentificationNumberPage(Index(1))) mustBe None
      userAnswersWithEquipments.get(IdentificationNumberPage(Index(2))) mustBe None

      whenReady(transformer.transform(userAnswersWithEquipments)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(ContainerIdentificationNumberPage(Index(0))) mustBe Some("container id 1")
          updatedUserAnswers.get(ContainerIdentificationNumberPage(Index(1))) mustBe None
          updatedUserAnswers.get(ContainerIdentificationNumberPage(Index(2))) mustBe Some("container id 3")
      }
    }
  }
}
