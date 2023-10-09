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

import base.SpecBase
import models.messages.Authorisation
import models.messages.AuthorisationType.{C521, Other}
import play.api.libs.json.Json

class AuthorisationSpec extends SpecBase {

  "Authorisation" - {

    "must deserialise for C521" in {

      val data =
        Json.parse("""
            |{
            |  "type" : "C521",
            |  "referenceNumber": "AB123"
            |}
            |""".stripMargin)

      val expectedResult = Authorisation(C521, "AB123")

      data.validate[Authorisation].asOpt.value mustBe expectedResult
    }

    "must deserialise for other" in {

      val data =
        Json.parse("""
            |{
            |  "type" : "otherType",
            |  "referenceNumber": "AB123"
            |}
            |""".stripMargin)

      val expectedResult = Authorisation(Other("otherType"), "AB123")

      data.validate[Authorisation].asOpt.value mustBe expectedResult
    }
  }

}
