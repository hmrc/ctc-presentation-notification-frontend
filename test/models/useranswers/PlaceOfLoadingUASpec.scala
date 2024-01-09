/*
 * Copyright 2024 HM Revenue & Customs
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

package models.useranswers

import base.SpecBase
import generators.Generators
import models.messages.PlaceOfLoading
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading._
import services.CheckYourAnswersReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlaceOfLoadingUASpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockService = mock[CheckYourAnswersReferenceDataService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  "when no place of loading data captured in IE170" - {
    "must present IE015 data" - {
      "when UN/LOCODE is answered" - {
        "and extra information not provided" in {
          forAll(nonEmptyString) {
            unLocode =>
              beforeEach()

              val placeOfLoading = PlaceOfLoading(
                UNLocode = Some(unLocode),
                country = None,
                location = None
              )

              val result = PlaceOfLoadingUA(emptyUserAnswers, Some(placeOfLoading), mockService)

              result.addUnlocodeYesNo mustBe Some(true)
              result.unlocode mustBe Some(unLocode)
              result.addExtraInformationYesNo mustBe Some(false)
              result.country.futureValue mustBe None
              result.location mustBe None

              verifyNoInteractions(mockService)
          }
        }

        "and extra information provided" in {
          forAll(nonEmptyString, arbitrary[Country], nonEmptyString) {
            (unLocode, country, location) =>
              beforeEach()

              val countryCode = country.code.code

              val placeOfLoading = PlaceOfLoading(
                UNLocode = Some(unLocode),
                country = Some(countryCode),
                location = Some(location)
              )

              when(mockService.getCountry(any())(any()))
                .thenReturn(Future.successful(country))

              val result = PlaceOfLoadingUA(emptyUserAnswers, Some(placeOfLoading), mockService)

              result.addUnlocodeYesNo mustBe Some(true)
              result.unlocode mustBe Some(unLocode)
              result.addExtraInformationYesNo mustBe Some(true)
              result.country.futureValue mustBe Some(country)
              result.location mustBe Some(location)

              verify(mockService).getCountry(eqTo(countryCode))(any())
          }
        }
      }

      "when UN/LOCODE is not answered" in {
        forAll(nonEmptyString, arbitrary[Country], nonEmptyString) {
          (countryCode, country, location) =>
            beforeEach()

            val countryCode = country.code.code

            val placeOfLoading = PlaceOfLoading(
              UNLocode = None,
              country = Some(countryCode),
              location = Some(location)
            )

            when(mockService.getCountry(any())(any()))
              .thenReturn(Future.successful(country))

            val result = PlaceOfLoadingUA(emptyUserAnswers, Some(placeOfLoading), mockService)

            result.addUnlocodeYesNo mustBe Some(false)
            result.unlocode mustBe None
            result.addExtraInformationYesNo mustBe None
            result.country.futureValue mustBe Some(country)
            result.location mustBe Some(location)

            verify(mockService).getCountry(eqTo(countryCode))(any())
        }
      }
    }
  }

  "when place of loading data captured in IE170" - {
    "must present IE170 data" - {
      "when UN/LOCODE is answered" - {
        "and extra information not provided" in {
          forAll(nonEmptyString) {
            unLocode =>
              beforeEach()

              val userAnswers = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
                .setValue(UnLocodePage, unLocode)
                .setValue(AddExtraInformationYesNoPage, false)

              val result = PlaceOfLoadingUA(userAnswers, None, mockService)

              result.addUnlocodeYesNo mustBe Some(true)
              result.unlocode mustBe Some(unLocode)
              result.addExtraInformationYesNo mustBe Some(false)
              result.country.futureValue mustBe None
              result.location mustBe None

              verifyNoInteractions(mockService)
          }
        }

        "and extra information provided" in {
          forAll(nonEmptyString, arbitrary[Country], nonEmptyString) {
            (unLocode, country, location) =>
              beforeEach()

              val userAnswers = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
                .setValue(UnLocodePage, unLocode)
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val result = PlaceOfLoadingUA(userAnswers, None, mockService)

              result.addUnlocodeYesNo mustBe Some(true)
              result.unlocode mustBe Some(unLocode)
              result.addExtraInformationYesNo mustBe Some(true)
              result.country.futureValue mustBe Some(country)
              result.location mustBe Some(location)

              verifyNoInteractions(mockService)
          }
        }
      }

      "when UN/LOCODE is not answered" in {
        forAll(arbitrary[Country], nonEmptyString) {
          (country, location) =>
            beforeEach()

            val userAnswers = emptyUserAnswers
              .setValue(AddUnLocodeYesNoPage, false)
              .setValue(CountryPage, country)
              .setValue(LocationPage, location)

            val result = PlaceOfLoadingUA(userAnswers, None, mockService)

            result.addUnlocodeYesNo mustBe Some(false)
            result.unlocode mustBe None
            result.addExtraInformationYesNo mustBe None
            result.country.futureValue mustBe Some(country)
            result.location mustBe Some(location)

            verifyNoInteractions(mockService)
        }
      }
    }
  }

}
