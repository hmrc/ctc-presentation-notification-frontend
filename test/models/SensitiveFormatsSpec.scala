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
      "r5/szDgj+BL50TfhgcNlvztZJZWWvycri43GeiUy/gAodIUQaIQHKD1UCAn0HanLuOydhRnBr25oMBbs6oJDDIX3/24UDkRMtJHB6HxWOzBQBNRoGfsv6+LVaxgDd3YJtuk+Jphg/WsSmep3yA4DM6W6TE2BYY608Mf2BYcVa8knOugd7xjooLg1cPNA8pfiJT5pEqzm+Ex7cwrmxQJO7sMerkRYHEA1I2WW7rx0IT47QhnAFJ9iLwz/0wnbmIWh608iZxB07Mw5b3PkRAAEVSIojmkQ7wPxG0h4NtfIS8/wnOkhmw/yVLR8jHJM9IiW3m0moVK6158mBWooekqED2DuEQh4/QaaCLAztNtkEM2AT/Q3MLFhJgVPkqFY1Rolav7urC+dwkcTCja4pTH/WJL+gpqjnCu90U2PZL8Sn9yDkr58583RYBHPEBabXt7Z7ph2/UYVLm6RjxJ7FDh3QRrdGdAWAi3PvUmCSkudG5KEMFogvYmUPpC/+BE1gJv9JYtwr28rFHI="

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
                "goodsItemsNo1",
                1,
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
