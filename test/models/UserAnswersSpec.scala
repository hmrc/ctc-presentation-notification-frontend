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
import pages.QuestionPage
import play.api.libs.json.{Format, JsPath, JsValue, Json}
import play.api.test.Helpers.running

import java.time.Instant
import scala.util.Try

class UserAnswersSpec extends SpecBase {

  private val testPageAnswer  = "foo"
  private val testPageAnswer2 = "bar"
  private val testPagePath    = "testPath"

  private val testCleanupPagePath   = "testCleanupPagePath"
  private val testCleanupPageAnswer = "testCleanupPageAnswer"

  case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testPagePath

    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      value match {
        case Some(_) => userAnswers.remove(TestCleanupPage)
        case _       => super.cleanup(value, userAnswers)
      }
  }

  case object TestCleanupPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testCleanupPagePath
  }

  "UserAnswers" - {

    "set" - {
      "must run cleanup when given a new answer" in {

        val userAnswers = emptyUserAnswers.setValue(TestCleanupPage, testCleanupPageAnswer)
        val result      = userAnswers.setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer
        )

        result.data mustEqual expectedData
      }

      "must run cleanup when given a different answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer2)

        val expectedData = Json.obj(
          testPagePath -> testPageAnswer2
        )

        result.data mustEqual expectedData
      }

      "must not run cleanup when given the same answer" in {

        val result = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)
          .setValue(TestCleanupPage, testCleanupPageAnswer)
          .setValue(TestPage, testPageAnswer)

        val expectedData = Json.obj(
          testCleanupPagePath -> testCleanupPageAnswer,
          testPagePath        -> testPageAnswer
        )

        result.data mustEqual expectedData
      }
    }

    "remove" - {
      "must remove data" in {

        val userAnswers = emptyUserAnswers
          .setValue(TestPage, testPageAnswer)

        val expectedUserAnswers = userAnswers.copy(data = Json.obj())

        val result = userAnswers.remove(TestPage).toOption.value.data

        result mustEqual expectedUserAnswers.data
      }
    }

    "formats" - {

      val userAnswers = UserAnswers(
        id = departureId,
        eoriNumber = eoriNumber,
        lrn = lrn.value,
        data = Json.obj(),
        lastUpdated = Instant.ofEpochMilli(1662546803472L),
        departureData = basicIe015
      )

      "when encryption enabled" - {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> true)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$departureId",
               |  "eoriNumber" : "${eoriNumber.value}",
               |  "lrn" : "$lrn",
               |  "data" : ${Json.toJson(Json.obj())(sensitiveFormats.jsObjectWrites)},
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  },
               |  "departureData" : "$encryptedIe015"
               |}
               |""".stripMargin)

          "read correctly" in {
            val result = json.as[UserAnswers]
            result mustEqual userAnswers
          }

          "write and read correctly" in {
            val result = Json.toJson(userAnswers).as[UserAnswers]
            result mustEqual userAnswers
          }
        }
      }

      "when encryption disabled" - {
        val app = guiceApplicationBuilder()
          .configure("encryption.enabled" -> false)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$departureId",
               |  "eoriNumber" : "${eoriNumber.value}",
               |  "lrn" : "$lrn",
               |  "data" : ${Json.toJson(Json.obj())(sensitiveFormats.jsObjectWrites)},
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  },
               |  "departureData" : "${basicIe015.toXML.toString()}"
               |}
               |""".stripMargin)

          "must read correctly" in {
            val result = json.as[UserAnswers]
            result mustEqual userAnswers
          }

          "write correctly" in {
            val result = Json.toJson(userAnswers)
            result mustEqual json
          }
        }
      }
    }
  }
}
