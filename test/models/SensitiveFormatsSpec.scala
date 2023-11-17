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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.Constants.EntrySummaryDeclarationSecurityDetails
import models.messages.{Commodity, Consignment, ConsignmentItem, HouseConsignment, MessageData, TransitOperation}
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
      "fnR9UUxFHzzjzJzKxHKBDyTgCA8gAbhqAB3jpmHnKoi/M1jgydbbrMTLaOmfPyOcPJLzAH6CU+Otx82ubTwLBFTKM0CTgRU7x0DZCuhgacpBIR108EFBCstwIHgCazd2PXZ9/RRmyG6irCyDe4y7cym6+7C/XXGjxqFYPUvCLL3fUq8bc7R/EelfGgAW1KrGJg2sD6sFoY9iIHRy+Dm9W2nHyGStYaK2wzRIJU+Ib/lGIiDnBr1jt4/FhtZhWq8JCRKK+uCF4fEZ+Y0izs6cq+FSaGOetRTgq3dJbmZG3vlrQ42VUp80DeSL/DJuKT3gcOr6JWXZUyERM0OdBEPftCQYAdy78SsuezjkNNAi+TPp7TFwQ8a0dpYLvQDoKJdyg+i7lbGsmDBP0H2eecknPH4t3qOoK3ctO771KGf6yNUq"

    val decryptedValue = MessageData(
      CustomsOfficeOfDeparture = "",
      TransitOperation = TransitOperation(None, None, EntrySummaryDeclarationSecurityDetails),
      Authorisation = None,
      Consignment = Consignment(
        containerIndicator = None,
        modeOfTransportAtTheBorder = None,
        TransportEquipment = None,
        LocationOfGoods = None,
        ActiveBorderTransportMeans = None,
        PlaceOfLoading = None,
        HouseConsignment = Seq(
          HouseConsignment(
            List(
              ConsignmentItem(
                Commodity("descOfGoods")
              )
            )
          )
        )
      ),
      CustomsOfficeOfDestination = "",
      CustomsOfficeOfExitForTransitDeclared = None,
      CustomsOfficeOfTransitDeclared = None
    )

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
