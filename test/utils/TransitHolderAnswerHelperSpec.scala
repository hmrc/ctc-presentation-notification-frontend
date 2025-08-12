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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.AddressType14
import generators.Generators
import models.reference.Country
import models.{Mode, RichAddressType14}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import services.CheckYourAnswersReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransitHolderAnswerHelperSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]

  "TransitHolderAnswerHelper should" - {

    "return `Do you know the transit holder’s EORI number?` row" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, eori) =>
          val answers = setTransitHolderEoriLens.replace(Some(eori))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.eoriYesNoRow

          result.key.value mustEqual "Do you know the transit holder’s EORI number?"
          result.value.value mustEqual "Yes"
          val actions = result.actions
          actions.size mustEqual 0
      }
    }

    "return EORI number row" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, eori) =>
          val answers = setTransitHolderEoriLens.replace(Some(eori))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.eoriRow

          result.get.key.value mustEqual "EORI number"
          result.get.value.value mustEqual eori
          val actions = result.get.actions
          actions.size mustEqual 0
      }
    }

    "return TIR identification row" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, tirIdentification) =>
          val answers = setTransitHolderTirIdentificationLens.replace(Some(tirIdentification))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.tirIdentificationRow

          result.get.key.value mustEqual "TIR holder’s identification number"
          result.get.value.value mustEqual tirIdentification
          val actions = result.get.actions
          actions.size mustEqual 0
      }
    }

    "return country row" in {
      forAll(arbitrary[Mode], arbitrary[AddressType14], arbitrary[Country]) {
        (mode, address, country) =>
          when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(country))
          val answers = setTransitHolderAddressLens.replace(Some(address.copy(country = country.code.code))).apply(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.countryRow.get.futureValue

          result.key.value mustEqual "Transit holder’s country"
          result.value.value mustEqual country.description
          val actions = result.actions
          actions.size mustEqual 0
      }
    }

    "return address row" in {
      forAll(arbitrary[Mode], arbitrary[AddressType14], arbitrary[Country]) {
        (mode, address, country) =>
          when(mockReferenceDataService.getCountry(any())(any())).thenReturn(Future.successful(country))
          val answers = setTransitHolderAddressLens.replace(Some(address))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.addressRow

          result.get.key.value mustEqual "Transit holder’s address"
          result.get.value.value mustEqual address.toDynamicAddress.toString
          val actions = result.get.actions
          actions.size mustEqual 0
      }
    }

    "return name row" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, name) =>
          val answers = setTransitHolderNameLens.replace(Some(name))(emptyUserAnswers)
          val helper  = new TransitHolderAnswerHelper(answers, departureId, mockReferenceDataService, mode)
          val result  = helper.nameRow

          result.get.key.value mustEqual "Transit holder’s name"
          result.get.value.value mustEqual name
          val actions = result.get.actions
          actions.size mustEqual 0
      }
    }

    "return section" in {
      forAll(arbitrary[Mode], nonEmptyString, arbitrary[AddressType14]) {
        (mode, str, address) =>
          val pipeline = setTransitHolderEoriLens.replace(Some(str)) andThen
            setTransitHolderTirIdentificationLens.replace(Some(str)) andThen
            setTransitHolderAddressLens.replace(Some(address)) andThen
            setTransitHolderAddressLens.replace(Some(address)) andThen
            setTransitHolderNameLens.replace(Some(str))

          val userAnswers = pipeline(emptyUserAnswers)
          val helper      = new TransitHolderAnswerHelper(userAnswers, departureId, mockReferenceDataService, mode)
          val section     = helper.transitHolderSection.futureValue

          section.sectionTitle.get mustEqual "Transit holder"
          section.rows.size mustEqual 6
      }
    }
  }
}
