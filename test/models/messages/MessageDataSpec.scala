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

import base.TestMessageData.{incompleteJsonValue, jsonValue, jsonValueNormalNoLimitDate, messageData}
import models.messages.AuthorisationType.{C521, C523, Other}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MessageDataSpec extends AnyFreeSpec with Matchers with OptionValues {

  "MessageDataSpec" - {

    "must deserialize" in {
      jsonValue.as[MessageData] mustBe messageData
    }

    "must return true when completed data sent through as simplified" in {
      jsonValue.as[MessageData].isDataCompleteSimplified mustBe true
    }

    "must return false when incomplete data sent through as simplified" in {
      incompleteJsonValue.as[MessageData].isDataCompleteSimplified mustBe false
    }

    "must return false when incomplete data sent through as simplified - missing limit date" in {
      jsonValueNormalNoLimitDate.as[MessageData].isDataCompleteSimplified mustBe false
    }

    "must return true when completed data sent through as normal" in {
      jsonValueNormalNoLimitDate.as[MessageData].isDataCompleteNormal mustBe true
    }

    "must return false when incomplete data sent through as normal" in {
      incompleteJsonValue.as[MessageData].isDataCompleteNormal mustBe false
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

    "hasAuthC523" - {

      "must return true when authorisations contain C523" in {

        val updatedMessageData: MessageData = messageData.copy(Authorisation =
          Some(
            Seq(
              Authorisation(C523, "CD456"),
              Authorisation(Other("otherValue"), "AB123")
            )
          )
        )

        updatedMessageData.hasAuthC523 mustBe true
      }

      "must return false" - {

        "when authorisations are not defined" in {

          val updatedMessageData = messageData.copy(Authorisation = None)

          updatedMessageData.hasAuthC523 mustBe false
        }

        "when authorisations are defined but without C523" in {

          val updatedMessageData: MessageData = messageData.copy(Authorisation =
            Some(
              Seq(Authorisation(Other("otherValue"), "AB123"))
            )
          )

          updatedMessageData.hasAuthC523 mustBe false
        }
      }
    }
  }
}
