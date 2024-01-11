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
import models.messages.{Commodity, Consignment, ConsignmentItem, HouseConsignment}
import models.reference.Item

class ConsignmentSpec extends SpecBase {

  "Consignment" - {

    "itemsToSelectable" - {

      "must concat ConsignmentItems from every house to list of items" in {

        val consignment = Consignment(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          Seq(
            HouseConsignment(
              List(
                ConsignmentItem(
                  "1",
                  1,
                  Commodity("desc1")
                ),
                ConsignmentItem(
                  "2",
                  2,
                  Commodity("desc2")
                )
              )
            ),
            HouseConsignment(
              List(
                ConsignmentItem(
                  "3",
                  3,
                  Commodity("desc3")
                ),
                ConsignmentItem(
                  "4",
                  4,
                  Commodity("desc4")
                )
              )
            )
          )
        )

        val expectedResult =
          Seq(
            Item(1, "desc1"),
            Item(2, "desc2"),
            Item(3, "desc3"),
            Item(4, "desc4")
          )

        consignment.allItems mustBe expectedResult
      }
    }
  }
}
