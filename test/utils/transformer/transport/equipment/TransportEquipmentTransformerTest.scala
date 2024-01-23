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
import models.Index
import pages.transport.equipment.AddAnotherTransportEquipmentPage

class TransportEquipmentTransformerTest extends SpecBase {

  val transformer = new TransportEquipmentTransformer()

  "TransportEquipmentTransformer" - {
    "must return updated answers with AddAnotherTransportEquipmentPage" in {
      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(AddAnotherTransportEquipmentPage(index)) mustBe None

      whenReady(transformer.transform(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddAnotherTransportEquipmentPage(index)) mustBe Some(true)
      }
    }
  }
}
