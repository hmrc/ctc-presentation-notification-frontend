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
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.libs.json.Json

class LocationOfGoodsIdentificationSpec extends SpecBase {
  private val mockFrontendAppConfig = mock[FrontendAppConfig]

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
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(false)
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
                .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(mockFrontendAppConfig)) mustEqual value
          }

        }

        "when phase 6" in {
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(true)
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
                .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(mockFrontendAppConfig)) mustEqual value
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
