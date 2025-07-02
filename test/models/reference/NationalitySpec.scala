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
import config.FrontendAppConfig
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class NationalitySpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Nationality" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val nationality = Nationality(code, description)
          Json.toJson(nationality) mustEqual Json.parse(s"""
                                                           |{
                                                           |  "code": "$code",
                                                           |  "description": "$description"
                                                           |}
                                                           |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val nationality = Nationality(code, description)
            Json
              .parse(s"""
                        |{
                        |  "code": "$code",
                        |  "description": "$description"
                        |}
                        |""".stripMargin)
              .as[Nationality] mustEqual nationality
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val nationality = Nationality(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "code": "$code",
                              |  "description": "$description"
                              |}
                              |""".stripMargin)
                    .as[Nationality](Nationality.reads(config)) mustEqual nationality
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val nationality = Nationality(code, description)
                  Json
                    .parse(s"""
                              |{
                              |  "key": "$code",
                              |  "value": "$description"
                              |}
                              |""".stripMargin)
                    .as[Nationality](Nationality.reads(config)) mustEqual nationality
              }
          }
        }
      }
    }

    "must convert to select item" in {
      forAll(arbitrary[Nationality], arbitrary[Boolean]) {
        (nationality, selected) =>
          nationality.toSelectItem(selected) mustEqual SelectItem(Some(nationality.code), s"${nationality.description} - ${nationality.code}", selected)
      }
    }

    "must format as string" in {
      forAll(arbitrary[Nationality]) {
        nationality =>
          nationality.toString mustEqual s"${nationality.description} - ${nationality.code}"
      }
    }
  }

}
