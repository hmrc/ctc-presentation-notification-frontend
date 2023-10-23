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

import base.TestMessageData.messageData
import base.{AppWithDefaultMockFixtures, SpecBase}
import models.messages.MessageData
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.running

class SensitiveFormatsSpec extends SpecBase with AppWithDefaultMockFixtures {

  "JsObject" - {
    val encryptedValue = "WFYrOuMf6WHDjHooyzED80QIGXMTPSHEjc3Kl8jPFRJFtHWV"
    val decryptedValue = Json.obj()

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
          }
        }
      }

      "when encryption disabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
            result mustBe decryptedValue
          }
        }
      }
    }

    "writes" - {
      "when encryption enabled" - {
        "must write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result must not be decryptedValue
          }
        }
      }

      "encryption disabled" - {
        "must write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
            result mustBe decryptedValue
          }
        }
      }
    }
  }

  "MessageData" - {
    val encryptedValue =
      "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"
    val decryptedValue = messageData

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[MessageData](sensitiveFormats.messageDataReads)
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[MessageData](sensitiveFormats.messageDataReads)
            result mustBe decryptedValue
          }
        }
      }

      "when encryption disabled" - {
        "must read an encrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(encryptedValue).as[MessageData](sensitiveFormats.messageDataReads)
            result mustBe decryptedValue
          }
        }

        "must read a decrypted value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue).as[MessageData](sensitiveFormats.messageDataReads)
            result mustBe decryptedValue
          }
        }
      }
    }

    "writes" - {
      "when encryption enabled" - {
        "must write and encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> true)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.messageDataWrites)
            result mustBe a[JsString]
          }
        }
      }

      "encryption disabled" - {
        "must write and not encrypt the value" in {
          val app = guiceApplicationBuilder()
            .configure("encryption.enabled" -> false)
            .build()

          running(app) {
            val sensitiveFormats = app.injector.instanceOf[SensitiveFormats]
            val result           = Json.toJson(decryptedValue)(sensitiveFormats.messageDataWrites)
            result mustBe a[JsObject]
          }
        }
      }
    }
  }
}
