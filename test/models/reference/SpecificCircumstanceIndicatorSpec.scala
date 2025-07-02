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

package models.reference

import base.SpecBase
import cats.data.NonEmptySet
import config.FrontendAppConfig
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.running

class SpecificCircumstanceIndicatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Country" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val value = SpecificCircumstanceIndicator(code, description)
          Json.toJson(value) mustEqual
            Json.parse(s"""
                          |{
                          |  "code": "$code",
                          |  "description": "$description"
                          |}
                          |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config                                               = app.injector.instanceOf[FrontendAppConfig]
              implicit val reads: Reads[SpecificCircumstanceIndicator] = SpecificCircumstanceIndicator.reads(config)
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = SpecificCircumstanceIndicator(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "code": "$code",
                              |  "description": "$description"
                              |}
                              |""".stripMargin)
                    .as[SpecificCircumstanceIndicator] mustEqual value
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config                                               = app.injector.instanceOf[FrontendAppConfig]
              implicit val reads: Reads[SpecificCircumstanceIndicator] = SpecificCircumstanceIndicator.reads(config)
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val value = SpecificCircumstanceIndicator(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "key": "$code",
                              |  "value": "$description"
                              |}
                              |""".stripMargin)
                    .as[SpecificCircumstanceIndicator] mustEqual value
              }
          }
        }
      }

      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val value = SpecificCircumstanceIndicator(code, description)
            Json
              .parse(s"""
                        |{
                        |  "code": "$code",
                        |  "description": "$description"
                        |}
                        |""".stripMargin)
              .as[SpecificCircumstanceIndicator] mustEqual value
        }
      }
    }

    "must format as string" in {
      forAll(arbitrary[SpecificCircumstanceIndicator]) {
        value =>
          value.toString mustEqual s"${value.code} - ${value.description}"
      }
    }

    "must order" in {
      val value1 = SpecificCircumstanceIndicator("XXX", "Authorised economic operators")
      val value2 = SpecificCircumstanceIndicator("A20", "Express consignments in the context of exit summary declarations")

      val values = NonEmptySet.of(value1, value2)

      val result = values.toNonEmptyList.toList

      result mustEqual Seq(
        value2,
        value1
      )
    }
  }
}
