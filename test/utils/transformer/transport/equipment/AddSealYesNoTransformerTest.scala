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

package utils.transformer.transport.equipment

import base.SpecBase
import models.UserAnswers
import models.messages.TransportEquipment
import pages.transport.equipment.index.AddSealYesNoPage

class AddSealYesNoTransformerTest extends SpecBase {

  val transformer = new AddSealYesNoTransformer()

  "AddSealYesNoTransformer" - {
    "when seals present must return updated answers with AddSealYesNoPage as true" in {
      val userAnswers = emptyUserAnswers
      userAnswers.get(AddSealYesNoPage(equipmentIndex)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddSealYesNoPage(equipmentIndex)) mustBe Some(true)
      }
    }

    "when seals not present must return updated answers with AddSealYesNoPage as false" in {
      val userAnswers = UserAnswers.setTransportEquipmentLens.set(Some(List(TransportEquipment("1", None, 0, None, None))))(emptyUserAnswers)

      userAnswers.get(AddSealYesNoPage(equipmentIndex)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddSealYesNoPage(equipmentIndex)) mustBe Some(false)
      }
    }
  }
}
