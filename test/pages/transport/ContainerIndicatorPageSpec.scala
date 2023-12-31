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

package pages.transport

import pages.behaviours.PageBehaviours

class ContainerIndicatorPageSpec extends PageBehaviours {

  "ContainerIndicatorPage" - {

    beRetrievable[Boolean](ContainerIndicatorPage)

    beSettable[Boolean](ContainerIndicatorPage)

    beRemovable[Boolean](ContainerIndicatorPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up ContainerSection" ignore {
          ??? // TODO: will be implemented after next page
        }
      }

      "when YES selected" - {
        "must do nothing" ignore {
          ??? // TODO: will be implemented after next page
        }
      }
    }

  }

}
