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

package pages.locationOfGoods

import models.DynamicAddress
import models.reference.Country
import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours

class CountryPageSpec extends PageBehaviours {

  "CountryPage" - {

    beRetrievable[Country](CountryPage)

    beSettable[Country](CountryPage)

    beRemovable[Country](CountryPage)

    "cleanup" - {
      "when answer changes" - {
        "must clean up address page" in {

          forAll(arbitrary[Country], arbitrary[Country], arbitrary[DynamicAddress]) {
            (country1, country2, dynamicAddress) =>
              val preChange = emptyUserAnswers
                .setValue(CountryPage, country1)
                .setValue(AddressPage, dynamicAddress)

              val postChange = preChange.setValue(CountryPage, country2)

              postChange.get(AddressPage) mustNot be(defined)

          }
        }
      }

      "when answer does not change" - {
        "must do nothing" in {
          forAll(arbitrary[Country], arbitrary[DynamicAddress]) {
            (country, dynamicAddress) =>
              val preChange = emptyUserAnswers
                .setValue(CountryPage, country)
                .setValue(AddressPage, dynamicAddress)

              val postChange = preChange.setValue(CountryPage, country)

              postChange.get(AddressPage) must be(defined)

          }
        }
      }
    }

  }
}
