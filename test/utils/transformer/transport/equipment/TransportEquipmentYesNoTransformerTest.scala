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
import generated.TransportEquipmentType03
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.AddTransportEquipmentYesNoPage

class TransportEquipmentYesNoTransformerTest extends SpecBase with Generators {

  val transformer = new TransportEquipmentYesNoTransformer()

  "TransportEquipmentYesNoTransformer" - {
    "when transport equipment present must return updated answers with AddTransportEquipmentYesNoPage as true" in {
      forAll(arbitrary[TransportEquipmentType03]) {
        transportEquipment =>
          val userAnswers = setTransportEquipmentLens.replace(
            Seq(transportEquipment)
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddTransportEquipmentYesNoPage).value mustEqual true
      }
    }

    "when transport equipment not present must return updated answers with AddTransportEquipmentYesNoPage as false" in {
      forAll(arbitrary[TransportEquipmentType03]) {
        transportEquipment =>
          val userAnswers = setTransportEquipmentLens.replace(
            Nil
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddTransportEquipmentYesNoPage).value mustEqual false
      }
    }
  }
}
