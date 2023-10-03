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

package models.messages

import base.TestMessageData
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class MessageDataSpec extends AnyFreeSpec with Matchers with OptionValues with TestMessageData {

  "MessageDataSpec" - {

    "must serialize" in {
      val ie015Data = Data(messageData)
      Json.toJsObject(ie015Data.data) mustBe jsonValue
    }

    "must deserialize" in {
      jsonValue.as[MessageData] mustBe messageData
    }

    "must return true when completed data sent through" in {
      jsonValue.as[MessageData].isDataComplete mustBe true
    }

    "must return false when incomplete data sent through" in {
      incompleteJsonValue.as[MessageData].isDataComplete mustBe false
    }
  }
}
