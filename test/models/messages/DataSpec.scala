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

package models.messages

import base.{SpecBase, TestMessageData}
import models.departureP5.DepartureMessageType
import play.api.libs.json.Json

class DataSpec extends SpecBase with TestMessageData {
  "must serialise" - {
    "when IE015" in {
      val jsonIE015 = Json.parse(s"""
          |{
          |  "type": "IE015",
          |  "body" : {
          |    "n1:CC015C": $jsonValue
          |  }
          |}
          |""".stripMargin)

      jsonIE015.as[Data](Data.reads(DepartureMessageType.DepartureNotification)) mustBe Data(
        messageData
      )
    }

    "when IE013" in {
      val jsonIE013 = Json.parse(s"""
           |{
           |  "type" : "IE013",
           |  "body" : {
           |    "n1:CC013C": $jsonValue
           |  }
           |}
           |""".stripMargin)

      jsonIE013.as[Data](Data.reads(DepartureMessageType.AmendmentSubmitted)) mustBe Data(
        messageData
      )
    }
  }
}
