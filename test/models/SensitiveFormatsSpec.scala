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
import models.messages._
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
      "mIbM/atO3ZhRAjHRlmRA/+BifQvy1dB2N5ZGaMhT+GOtBwAVwl61S0dNj/trZHk+SRRxV/O9DFQCVWItEqyrSkqkCleyEZF9LEGnQa+69jHiyAZBezqZGwrQObtfZbtboT2d/kqDIazVSUQBWUOZdW4H4SFQ2EvbaOEVKDCqwYtF546Iq0W55T0un9eMF6iuxdEsLmfPUGrlvy806QOsEce8jPOIrNutVHgj9UlvrxTn1lRrUboJ3KxZudhHXGswM9vW+Hw/37OZloHv8wriJgXQi+DRNGa0xPZPVu9uo7UkrfuRBBXdKWKZkZQFmA9VCFGNC7hmtEqa/228X6EhdQo2tHmas2xt7uHPmeXFF3AR/D9gbCNY6brlcUllUDOnXZ0CcgT7gg00abyVmM6ZWrm7LVdVDLec6X+/W/u98cFxWMORF1/C73ei92hUBHk5uGP+EoSmAZFaQd177lqxkcee3tILyQVTOE0eNr38IBUH+PiIFBdWLHvXhkaXM/3XPYCUdg9s+RD5NSth2bEQgfdKLosnhBJwwtFNrP3YmHzUTAjPGEziyN8YWFJg8DJei+3y1HhiOaQrOv9GwpQ77Hdknio+8IftK0NBD/Ll9WkG8sAY+F3NXFJIJNk="

    val decryptedValue = MessageData(
      CustomsOfficeOfDeparture = "",
      TransitOperation = TransitOperation(None, None, EntrySummaryDeclarationSecurityDetails),
      Authorisation = None,
      HolderOfTheTransitProcedure = HolderOfTheTransitProcedure(
        Some("identificationNumber"),
        None,
        None,
        None
      ),
      Representative = None,
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
