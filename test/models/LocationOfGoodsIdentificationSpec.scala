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
import config.FrontendAppConfig
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class LocationOfGoodsIdentificationSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TypeOfLocation" - {

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = LocationOfGoodsIdentification(code, description)
            Json
              .parse(s"""
                        |{
                        |  "qualifier": "$code",
                        |  "description": "$description"
                        |}
                        |""".stripMargin)
              .as[LocationOfGoodsIdentification] mustEqual value
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = LocationOfGoodsIdentification(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "qualifier": "$code",
                              |  "description": "$description"
                              |}
                              |""".stripMargin)
                    .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(config)) mustEqual value
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = LocationOfGoodsIdentification(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "key": "$code",
                              |  "value": "$description"
                              |}
                              |""".stripMargin)
                    .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(config)) mustEqual value
              }
          }
        }
      }
    }

    "must serialise" in {

      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = LocationOfGoodsIdentification(code, description)
          Json.toJson(value) mustEqual Json.parse(s"""
                                                              |{
                                                              |  "qualifier": "$code",
                                                              |  "description": "$description"
                                                              |}
                                                              |""".stripMargin)
      }
    }
  }
}
