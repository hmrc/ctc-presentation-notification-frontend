/*
 * Copyright 2025 HM Revenue & Customs
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
import generated.TransitOperationType03
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.LimitDatePage
import scalaxb.XMLCalendar

import java.time.LocalDate

class TransitOperationTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[TransitOperationTransformer]

  "must transform data" in {
    val limitDate = XMLCalendar("2022-02-03T08:45:00.000000")

    forAll(
      arbitrary[TransitOperationType03].map(
        _.copy(
          limitDate = Some(limitDate)
        )
      )
    ) {
      transitOperation =>
        val result = transformer.transform(transitOperation).apply(emptyUserAnswers).futureValue

        result.get(LimitDatePage).value mustEqual LocalDate.parse("2022-02-03")
    }
  }
}
