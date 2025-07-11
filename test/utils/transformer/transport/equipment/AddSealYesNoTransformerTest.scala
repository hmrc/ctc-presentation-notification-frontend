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
import generated.{SealType01, TransportEquipmentType03}
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import pages.transport.equipment.index.AddSealYesNoPage

class AddSealYesNoTransformerTest extends SpecBase with Generators {

  val transformer = new AddSealYesNoTransformer()

  "AddSealYesNoTransformer" - {
    "when seals present must return updated answers with AddSealYesNoPage as true" in {
      forAll(arbitrary[TransportEquipmentType03], arbitrary[SealType01]) {
        (transportEquipment, seal) =>
          val userAnswers = setTransportEquipmentLens.replace(
            Seq(transportEquipment.copy(Seal = Seq(seal)))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddSealYesNoPage(Index(0))) mustBe Some(true)
      }
    }

    "when seals not present must return updated answers with AddSealYesNoPage as false" in {
      forAll(arbitrary[TransportEquipmentType03]) {
        transportEquipment =>
          val userAnswers = setTransportEquipmentLens.replace(
            Seq(transportEquipment.copy(Seal = Nil))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddSealYesNoPage(Index(0))) mustBe Some(false)
      }
    }
  }
}
