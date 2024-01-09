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

///*
// * Copyright 2024 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package utils
//
//import base.SpecBase
//import generators.Generators
//import models.{Mode, UserAnswers}
//import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
//import org.scalacheck.Arbitrary.arbitrary
//import pages.loading.{AddExtraInformationYesNoPage, AddUnLocodeYesNoPage, CountryPage, LocationPage, UnLocodePage}
//import services.CheckYourAnswersReferenceDataService
//
//import java.time.Instant
//import scala.concurrent.ExecutionContext.Implicits.global
//import play.api.libs.json.Json
//import base.TestMessageData.{allOptionsNoneJsonValue, messageData}
//import models.messages.MessageData
//import models.reference.Country
//
//class PlaceOfLoadingAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
//  "PlaceOfLoadingAnswersHelper" - {
//    val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]
//    "addUnlocodeYesNo" - {
//      "must return None when no AddUnlocodeYesNo in ie15/170" - {
//        s"when $AddUnLocodeYesNoPage undefined" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithUnlocodeAddYesNoUserAnswers =
//                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
//              val helper = new PlaceOfLoadingAnswersHelper(ie015WithUnlocodeAddYesNoUserAnswers, departureId, mockReferenceDataService, mode)
//              val result = helper.addUnlocodeYesNo
//              result mustBe None
//          }
//        }
//      }
//
//      "must return Some(Row)" - {
//        s"when $AddUnLocodeYesNoPage defined in the ie170" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val answers = emptyUserAnswers
//                .setValue(AddUnLocodeYesNoPage, true)
//              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mockReferenceDataService, mode)
//              val result = helper.addUnlocodeYesNo.get
//
//              result.key.value mustBe s"Do you want to add a UN/LOCODE for the place of loading?"
//              result.value.value mustBe "Yes"
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.AddUnLocodeYesNoController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "if you want a UN/LOCODE for the place of loading"
//              action.id mustBe "change-add-unlocode"
//          }
//        }
//
//        s"when $AddUnLocodeYesNoPage defined in the ie15" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val helper                      = new PlaceOfLoadingAnswersHelper(ie015WithLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result                      = helper.addUnlocodeYesNo.get
//
//              result.key.value mustBe s"Do you want to add a UN/LOCODE for the place of loading?"
//              result.value.value mustBe "Yes"
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.AddUnLocodeYesNoController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "if you want a UN/LOCODE for the place of loading"
//              action.id mustBe "change-add-unlocode"
//          }
//        }
//      }
//    }
//
//    "unlocode" - {
//      "must return None when no unlocode in ie15/170" - {
//        s"when $UnLocodePage undefined" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithLoadingUserAnswers =
//                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
//              val helper = new PlaceOfLoadingAnswersHelper(ie015WithLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result = helper.unlocode
//              result mustBe None
//          }
//        }
//      }
//
//      "must return Some(Row)" - {
//        s"when $UnLocodePage defined in the ie170" in {
//          forAll(arbitrary[Mode], arbitraryUnLocode.arbitrary) {
//            (mode, unlocode) =>
//              val answers = emptyUserAnswers
//                .setValue(UnLocodePage, unlocode)
//              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mockReferenceDataService, mode)
//              val result = helper.unlocode.get
//
//              result.key.value mustBe s"UN/LOCODE"
//              result.value.value mustBe unlocode
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.UnLocodeController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "UN/LOCODE for the place of loading"
//              action.id mustBe "change-unlocode"
//          }
//        }
//
//        s"when $UnLocodePage defined in the ie15" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val helper                      = new PlaceOfLoadingAnswersHelper(ie015WithLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result                      = helper.unlocode.get
//
//              result.key.value mustBe s"UN/LOCODE"
//              result.value.value mustBe messageData.Consignment.PlaceOfLoading.flatMap(_.UNLocode).get
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.UnLocodeController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "UN/LOCODE for the place of loading"
//              action.id mustBe "change-unlocode"
//          }
//        }
//      }
//    }
//
//    "addExtraInformationYesNo" - {
//      "must return None when no addExtraInformation in ie15/170" - {
//        s"when $AddExtraInformationYesNoPage undefined" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithNoLoadingUserAnswers =
//                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
//              val helper = new PlaceOfLoadingAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result = helper.addExtraInformationYesNo
//              result mustBe None
//          }
//        }
//      }
//
//      "must return Some(Row)" - {
//        s"when $AddExtraInformationYesNoPage defined in the ie170" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val answers = emptyUserAnswers
//                .setValue(AddExtraInformationYesNoPage, true)
//              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mockReferenceDataService, mode)
//              val result = helper.addExtraInformationYesNo.get
//
//              result.key.value mustBe s"Do you want to add extra information for the place of loading?"
//              result.value.value mustBe "Yes"
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.AddExtraInformationYesNoController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "if you want to add extra information for the place of loading"
//              action.id mustBe "change-add-extra-information"
//          }
//        }
//
//        s"when $AddExtraInformationYesNoPage defined in the ie15" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val helper                      = new PlaceOfLoadingAnswersHelper(ie015WithLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result                      = helper.addExtraInformationYesNo.get
//
//              result.key.value mustBe s"Do you want to add extra information for the place of loading?"
//              result.value.value mustBe "Yes"
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.AddExtraInformationYesNoController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "if you want to add extra information for the place of loading"
//              action.id mustBe "change-add-extra-information"
//          }
//        }
//      }
//    }
//
//    "country" - {
//      "must return None" - {
//        "when no country in ie15/170" in {
//          forAll(arbitrary[Mode], arbitrary[Country]) {
//            (mode, countryType) =>
//              val ie015WithNoLoadingUserAnswers =
//                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
//              val helper = new PlaceOfLoadingAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result = helper.countryTypeRow(countryType.description)
//              result mustBe None
//          }
//        }
//
//        "when country defined in the ie15, Place of Loading defined in the ie170, but country undefined in the ie170" in {
//          forAll(arbitrary[Mode], nonEmptyString, arbitrary[Country]) {
//            (mode, unLocode, country) =>
//              val initialAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val userAnswers    = initialAnswers.setValue(UnLocodePage, unLocode)
//              val helper         = new PlaceOfLoadingAnswersHelper(userAnswers, departureId, mockReferenceDataService, mode)
//              val result         = helper.countryTypeRow(country.description)
//              result mustBe None
//          }
//        }
//      }
//
//      "must return Some(Row)" - {
//        s"when $CountryPage defined in the ie170" in {
//          forAll(arbitrary[Mode], arbitrary[Country]) {
//            (mode, country) =>
//              val answers = emptyUserAnswers.setValue(CountryPage, country)
//              val helper  = new PlaceOfLoadingAnswersHelper(answers, departureId, mockReferenceDataService, mode)
//              val result  = helper.countryTypeRow(country.description).get
//
//              result.key.value mustBe s"Country"
//              result.value.value mustBe country.description
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.CountryController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "country for the place of loading"
//              action.id mustBe "change-country"
//          }
//        }
//
//        s"when $CountryPage defined in the ie15" in {
//          forAll(arbitrary[Mode], arbitrary[Country]) {
//            (mode, countryType) =>
//              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val helper                      = new PlaceOfLoadingAnswersHelper(ie015WithLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result                      = helper.countryTypeRow(countryType.description).get
//
//              result.key.value mustBe s"Country"
//              //TODO: Change once we pull ref data to format country answer in the 15
//              result.value.value mustBe countryType.description
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.CountryController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "country for the place of loading"
//              action.id mustBe "change-country"
//          }
//        }
//      }
//    }
//
//    "location" - {
//      "must return None" - {
//        "when no location in ie15/170" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithNoLoadingUserAnswers =
//                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
//              val helper = new PlaceOfLoadingAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mockReferenceDataService, mode)
//              val result = helper.location
//              result mustBe None
//          }
//        }
//
//        "when location defined in the ie15, Place of Loading defined in the ie170, but location undefined in the ie170" in {
//          forAll(arbitrary[Mode], nonEmptyString) {
//            (mode, unLocode) =>
//              val initialAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val userAnswers    = initialAnswers.setValue(UnLocodePage, unLocode)
//              val helper         = new PlaceOfLoadingAnswersHelper(userAnswers, departureId, mockReferenceDataService, mode)
//              val result         = helper.location
//              result mustBe None
//          }
//        }
//      }
//
//      "must return Some(Row)" - {
//        s"when $LocationPage defined in the ie170" in {
//          forAll(arbitrary[Mode], nonEmptyString) {
//            (mode, location) =>
//              val answers = emptyUserAnswers
//                .setValue(LocationPage, location)
//              val helper = new PlaceOfLoadingAnswersHelper(answers, departureId, mockReferenceDataService, mode)
//              val result = helper.location.get
//
//              result.key.value mustBe s"Location"
//              result.value.value mustBe location
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.LocationController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "location for the place of loading"
//              action.id mustBe "change-location"
//          }
//        }
//
//        s"when $LocationPage defined in the ie15" in {
//          forAll(arbitrary[Mode]) {
//            mode =>
//              val ie015WithContainerIndicatorUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
//              val helper                                 = new PlaceOfLoadingAnswersHelper(ie015WithContainerIndicatorUserAnswers, departureId, mockReferenceDataService, mode)
//              val result                                 = helper.location.get
//
//              result.key.value mustBe s"Location"
//              result.value.value mustBe messageData.Consignment.PlaceOfLoading.flatMap(_.location).get
//              val actions = result.actions.get.items
//              actions.size mustBe 1
//              val action = actions.head
//              action.content.value mustBe "Change"
//              action.href mustBe controllers.loading.routes.LocationController.onPageLoad(departureId, mode).url
//              action.visuallyHiddenText.get mustBe "location for the place of loading"
//              action.id mustBe "change-location"
//          }
//        }
//      }
//    }
//
//  }
//}
