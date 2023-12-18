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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class RichJsObjectSpec extends AnyFreeSpec with Matchers {

  "removeObjectStandard" - {
    "should remove a field at the specified path" in {
      val originalJson = Json.obj("name" -> "John", "age" -> 25, "address" -> Json.obj("city" -> "New York"))

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath \ "age")

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual Json.obj("name" -> "John", "address" -> Json.obj("city" -> "New York"))
    }

    "should handle a non-existing path gracefully" in {
      val originalJson = Json.obj("name" -> "John", "age" -> 25, "address" -> Json.obj("city" -> "New York"))

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath \ "nonexistent")

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual originalJson // No change expected
    }

    "should handle an empty path gracefully" in {
      val originalJson = Json.obj("name" -> "John", "age" -> 25, "address" -> Json.obj("city" -> "New York"))

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath)

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual originalJson // No change expected
    }

    "should handle removing nested fields" in {
      val originalJson = Json.obj("person" -> Json.obj("name" -> "John", "age" -> 25))

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath \ "person" \ "age")

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual Json.obj("person" -> Json.obj("name" -> "John"))
    }

    "should handle removing from an empty object" in {
      val originalJson = Json.obj()

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath \ "nonexistent")

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual originalJson // No change expected
    }

    "should handle removing from an empty path in an object" in {
      val originalJson = Json.obj()

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath)

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual originalJson // No change expected
    }

    "should handle removing from an empty path in a nested object" in {
      val originalJson = Json.obj("nested" -> Json.obj())

      val updatedJsonResult: JsResult[JsObject] = originalJson.removeObjectStandard(JsPath \ "nested")

      updatedJsonResult shouldBe a[JsSuccess[_]]
      val updatedJson = updatedJsonResult.get
      updatedJson shouldEqual Json.obj() // Empty object expected
    }
  }
}
