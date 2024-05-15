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

package utils.transformer.transport

import base.SpecBase
import generators.Generators
import pages.transport.LimitDatePage
import scalaxb.XMLCalendar

import java.time.LocalDate

class LimitDateTransformerTest extends SpecBase with Generators {
  val transformer = new LimitDateTransformer()

  "LimitDateTransformer" - {
    "must return updated answers with LimitDatePage" in {
      val limitDate = XMLCalendar("2022-02-03T08:45:00.000000")
      val userAnswers = setLimitDateLens.set(
        Some(limitDate)
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(LimitDatePage).value mustBe LocalDate.parse("2022-02-03")
    }

    "must not update if limit date is None" in {
      val userAnswers = setLimitDateLens.set(
        None
      )(emptyUserAnswers)

      val result = transformer.transform.apply(userAnswers).futureValue
      result.get(LimitDatePage) mustBe None
    }
  }
}
