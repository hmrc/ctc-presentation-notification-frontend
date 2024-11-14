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

package models.departureP5

import base.SpecBase
import generators.Generators
import models.departureP5.MessageType.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class MessageTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "MessageType" - {
    "must deserialise" - {
      "when a JsString" - {
        "when IE015" in {
          val result = JsString("IE015").as[MessageType]
          result mustBe DeclarationData
        }

        "when IE013" in {
          val result = JsString("IE013").as[MessageType]
          result mustBe DeclarationAmendment
        }

        "when IE170" in {
          val result = JsString("IE170").as[MessageType]
          result mustBe PresentationForThePreLodgedDeclaration
        }

        "when IE928" in {
          val result = JsString("IE928").as[MessageType]
          result mustBe PositiveAcknowledgement
        }

        "when IE004" in {
          val result = JsString("IE004").as[MessageType]
          result mustBe AmendmentAcceptance
        }

        "when IE060" in {
          val result = JsString("IE060").as[MessageType]
          result mustBe ControlDecisionNotification
        }

        "when something else" in {
          forAll(nonEmptyString) {
            value =>
              val result = JsString(value).as[MessageType]
              result mustBe Other(value)
          }
        }
      }
    }

    "must fail to deserialise" - {
      "when not a JsString" - {
        val result = Json.obj("foo" -> "bar").validate[MessageType]
        result mustBe a[JsError]
      }
    }
  }
}
