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

import base.TestMessageData.{incompleteJsonValue, jsonValue, messageData}
import models.messages.AuthorisationType.{C521, Other}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MessageDataSpec extends AnyFreeSpec with Matchers with OptionValues {

  "MessageDataSpec" - {

    "must deserialize" in {
      jsonValue.as[MessageData] mustBe messageData
    }

    "must return true when completed data sent through" in {
      jsonValue.as[MessageData].isDataComplete mustBe true
    }

    "must return false when incomplete data sent through" in {
      incompleteJsonValue.as[MessageData].isDataComplete mustBe false
    }

    "isSimplified" - {

      "must return true when authorisations contain C521" in {

        val updatedMessageData: MessageData = messageData.copy(Authorisation =
          Some(
            Seq(
              Authorisation(C521, "AB123"),
              Authorisation(Other("otherValue"), "CD123")
            )
          )
        )

        updatedMessageData.isSimplified mustBe true
      }

      "must return false" - {

        "when authorisations are not defined" in {

          val updatedMessageData = messageData.copy(Authorisation = None)

          updatedMessageData.isSimplified mustBe false
        }

        "when authorisations are defined but without C521" in {

          val updatedMessageData: MessageData = messageData.copy(Authorisation =
            Some(
              Seq(Authorisation(Other("otherValue"), "AB123"))
            )
          )

          updatedMessageData.isSimplified mustBe false
        }
      }
    }
  }
}
