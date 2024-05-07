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

package utils

import base.SpecBase
import generators.Generators
import models.Mode
import models.messages.Address
import models.reference.{Country, CountryCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.CheckYourAnswersReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransitHolderAnswerHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]

  "TransitHolderAnswerHelper should" - {

    "return `Do you know the transit holder’s EORI number?` row" in {
      forAll(arbitrary[Mode], arbitrary[Option[String]]) {
        (mode, optionalEori) =>
          val answers = setTransitHolderEoriLens.set(optionalEori)(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.eoriYesNoRow

          result.key.value mustBe "Do you know the transit holder’s EORI number?"
          result.value.value mustBe optionalEori
            .map(
              _ => "Yes"
            )
            .getOrElse("No")
          val actions = result.actions
          actions.size mustBe 0
      }
    }

    "return EORI number row" in {
      forAll(arbitrary[Mode], arbitrary[Option[String]]) {
        (mode, optionalEori) =>
          val answers = setTransitHolderEoriLens.set(optionalEori)(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.eoriRow

          optionalEori match {
            case Some(eori) =>
              result.get.key.value mustBe "EORI number"
              result.get.value.value mustBe eori
              val actions = result.get.actions
              actions.size mustBe 0
            case None => result mustBe None
          }
      }
    }

    "return TIR identification row" in {
      forAll(arbitrary[Mode], arbitrary[Option[String]]) {
        (mode, optionalTirIdentification) =>
          val answers = setTransitHolderTirIdentificationLens.set(optionalTirIdentification)(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.tirIdentificationRow

          optionalTirIdentification match {
            case Some(identification) =>
              result.get.key.value mustBe "TIR holder’s identification number"
              result.get.value.value mustBe identification
              val actions = result.get.actions
              actions.size mustBe 0
            case None => result mustBe None
          }
      }
    }

    "return country row" in {
      forAll(arbitrary[Mode], arbitrary[Country]) {
        (mode, country) =>
          when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(country))
          val answers = setTransitHolderAddressLens.set(Address("Address Line 1", Some("NE53KL"), "Newcastle", country.code.code))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val row     = helper.countryRow

          whenReady(row.get) {
            result =>
              result.key.value mustBe "Transit holder’s country"
              result.value.value mustBe country.description
              val actions = result.actions
              actions.size mustBe 0
          }
      }
    }

    "return address row" in {
      forAll(arbitrary[Mode], arbitrary[Address]) {
        (mode, address) =>
          when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(Country(CountryCode(address.country), "description")))
          val answers = setTransitHolderAddressLens.set(address)(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.addressRow

          result.get.key.value mustBe "Transit holder’s address"
          result.get.value.value mustBe address.toDynamicAddress.toString
          val actions = result.get.actions
          actions.size mustBe 0
      }
    }

    "return name row" in {
      forAll(arbitrary[Mode], arbitrary[String]) {
        (mode, name) =>
          val answers = setTransitHolderNameLens.set(Some(name))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.nameRow

          result.get.key.value mustBe "Transit holder’s name"
          result.get.value.value mustBe name
          val actions = result.get.actions
          actions.size mustBe 0
      }
    }

    "return section" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val answerWithDefaultTransitHolderData = emptyUserAnswers
          val helper                             = new TransitHolderAnswerHelper(answerWithDefaultTransitHolderData, departureId, mockReferenceDataService, mode)
          val result                             = helper.transitHolderSection

          whenReady(result) {
            section =>
              section.sectionTitle.get mustBe "Transit holder"
              section.rows.size mustBe 6
          }
      }
    }
  }
}
