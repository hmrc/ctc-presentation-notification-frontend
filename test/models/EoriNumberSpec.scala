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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsString, Json}

class EoriNumberSpec extends AnyFreeSpec with Matchers {
  val eoriNumber: EoriNumber = EoriNumber("eoriNumber")

  "must serialise correctly" in {
    Json.toJson(eoriNumber) mustEqual JsString(eoriNumber.value)
  }

  "must deserialize correctly" in {
    JsString("eoriNumber").as[EoriNumber] mustEqual eoriNumber
  }
}
