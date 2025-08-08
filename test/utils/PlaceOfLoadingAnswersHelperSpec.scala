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

package utils

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import pages.loading.*

class PlaceOfLoadingAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  "PlaceOfLoadingAnswersHelper" - {

    "addUnlocodeYesNo" - {
      "must return None when no AddUnlocodeYesNo in ie15/170" - {
        s"when $AddUnLocodeYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PlaceOfLoadingAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.addUnlocodeYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddUnLocodeYesNoPage defined in the ie170" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mode)
              val result = helper.addUnlocodeYesNo.get

              result.key.value mustEqual s"Do you want to add a UN/LOCODE for the place of loading?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.loading.routes.AddUnLocodeYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you want a UN/LOCODE for the place of loading"
              action.id mustEqual "change-add-unlocode"
          }
        }
      }
    }

    "unlocode" - {
      "must return None when no unlocode in ie15/170" - {
        s"when $UnLocodePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PlaceOfLoadingAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.unlocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $UnLocodePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryUnLocode.arbitrary) {
            (mode, unlocode) =>
              val answers = emptyUserAnswers
                .setValue(UnLocodePage, unlocode.unLocodeExtendedCode)
              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mode)
              val result = helper.unlocode.get

              result.key.value mustEqual s"UN/LOCODE"
              result.value.value mustEqual unlocode.unLocodeExtendedCode
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.loading.routes.UnLocodeController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "UN/LOCODE for the place of loading"
              action.id mustEqual "change-unlocode"
          }
        }
      }
    }

    "addExtraInformationYesNo" - {
      "must return None when no addExtraInformation in ie15/170" - {
        s"when $AddExtraInformationYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PlaceOfLoadingAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.addExtraInformationYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddExtraInformationYesNoPage defined in the ie170" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddExtraInformationYesNoPage, true)
              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mode)
              val result = helper.addExtraInformationYesNo.get

              result.key.value mustEqual s"Do you want to add extra information for the place of loading?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.loading.routes.AddExtraInformationYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add extra information for the place of loading"
              action.id mustEqual "change-add-extra-information"
          }
        }
      }
    }

    "country" - {
      "must return None when no country in ie15/170" - {
        s"when $CountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PlaceOfLoadingAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.countryTypeRow
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CountryPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryCountry.arbitrary) {
            (mode, country) =>
              val answers = emptyUserAnswers
                .setValue(CountryPage, country)
              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mode)
              val result = helper.countryTypeRow.get

              result.key.value mustEqual s"Country"
              result.value.value mustEqual country.description
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.loading.routes.CountryController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "country for the place of loading"
              action.id mustEqual "change-country"
          }
        }
      }
    }

    "location" - {
      "must return None when no location in ie15/170" - {
        s"when $LocationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new PlaceOfLoadingAnswersHelper(emptyUserAnswers, departureId, mode)
              val result = helper.location
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LocationPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString.sample.value) {
            (mode, location) =>
              val answers = emptyUserAnswers
                .setValue(LocationPage, location)
              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mode)
              val result = helper.location.get

              result.key.value mustEqual s"Location"
              result.value.value mustEqual location
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.loading.routes.LocationController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "location for the place of loading"
              action.id mustEqual "change-location"
          }
        }
      }
    }

  }
}
