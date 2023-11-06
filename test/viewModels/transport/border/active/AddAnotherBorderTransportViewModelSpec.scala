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

package viewModels.transport.border.active

import base.SpecBase
import generators.Generators
import models.reference.transport.border.active.Identification
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import viewModels.transport.border.active.AddAnotherBorderTransportViewModel.AddAnotherBorderTransportViewModelProvider

class AddAnotherBorderTransportViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one border means of transport" in {
      forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
        (mode, identification, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(Index(0)), identification)
            .setValue(IdentificationNumberPage(Index(0)), identificationNumber)

          val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)
          result.listItems.length mustBe 1
          result.title mustBe "You have added 1 border means of transport"
          result.heading mustBe "You have added 1 border means of transport"
          result.legend mustBe "Do you want to add another border means of transport?"
          result.hint mustBe "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustBe "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }

    "when there are multiple border means of transport" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString, Gen.choose(2, frontendAppConfig.maxActiveBorderTransports)) {
        (mode, identification, identificationNumber, activeBorderTransports) =>
          val userAnswers = (0 until activeBorderTransports).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              acc
                .setValue(IdentificationPage(Index(i)), identification)
                .setValue(IdentificationNumberPage(Index(i)), identificationNumber)
          }

          val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)
          result.listItems.length mustBe activeBorderTransports
          result.title mustBe s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.heading mustBe s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.legend mustBe "Do you want to add another border means of transport?"
          result.hint mustBe "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustBe "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }
  }
}
