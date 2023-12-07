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
import base.TestMessageData.{allOptionsNoneJsonValue, messageData}
import generators.Generators
import models.messages.MessageData
import models.reference.{BorderMode, CustomsOffice}
import models.{Mode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loading.{AddExtraInformationYesNoPage, AddUnLocodeYesNoPage, CountryPage, LocationPage, UnLocodePage}
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, LimitDatePage}
import play.api.libs.json.Json

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class PresentationNotificationAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "PresentationNotificationAnswersHelper" - {

    "limitDate" - {
      "must return None when no limit date in ie15/170" - {
        s"when $LimitDatePage undefined" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val ie015WithNoLimitDateUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLimitDateUserAnswers, departureId, borderModes, mode)
              val result = helper.limitDate
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LimitDatePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice], arbitrary[Seq[BorderMode]]) {
            (mode, customsOffice, borderModes) =>
              when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(Some(customsOffice)))
              val limitDate = LocalDate.of(2000: Int, 1: Int, 8: Int)
              val answers = emptyUserAnswers
                .setValue(LimitDatePage, limitDate)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, borderModes, mode)
              val result = helper.limitDate.get

              result.key.value mustBe s"Estimated arrival date at the office of destination"
              result.value.value mustBe "8 January 2000"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.LimitDateController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "estimated arrival date at the office of destination"
              action.id mustBe "change-limit-date"
          }
        }

        s"when $LimitDatePage defined in the ie15" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice], arbitrary[Seq[BorderMode]]) {
            (mode, customsOffice, borderModes) =>
              when(mockCustomsOfficeService.getCustomsOfficeById(any())(any())).thenReturn(Future.successful(Some(customsOffice)))
              val ie015WithLimitDateUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                        = new PresentationNotificationAnswersHelper(ie015WithLimitDateUserAnswers, departureId, borderModes, mode)
              val result                        = helper.limitDate.get

              result.key.value mustBe s"Estimated arrival date at the office of destination"
              result.value.value mustBe "9 June 2023"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.LimitDateController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "estimated arrival date at the office of destination"
              action.id mustBe "change-limit-date"
          }
        }
      }
    }

    "containerIndicator" - {
      "must return None when no container indicator in ie15/170" - {
        s"when $ContainerIndicatorPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val ie015WithNoLimitDateUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLimitDateUserAnswers, departureId, borderModes, mode)
              val result = helper.containerIndicator
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIndicatorPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val answers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, borderModes, mode)
              val result = helper.containerIndicator.get

              result.key.value mustBe s"Are you using any shipping containers to transport the goods?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.ContainerIndicatorController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you are using any shipping containers to transport the goods"
              action.id mustBe "change-container-indicator"
          }
        }

        s"when $ContainerIndicatorPage defined in the ie15" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val ie015WithContainerIndicatorUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                                 = new PresentationNotificationAnswersHelper(ie015WithContainerIndicatorUserAnswers, departureId, borderModes, mode)
              val result                                 = helper.containerIndicator.get

              result.key.value mustBe s"Are you using any shipping containers to transport the goods?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.routes.ContainerIndicatorController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you are using any shipping containers to transport the goods"
              action.id mustBe "change-container-indicator"
          }
        }
      }
    }

    "addBorderModeOfTransportYesNo" - {
      "must return No" - {
        "when transport mode is undefined" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val ie015WithNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoUserAnswers, departureId, borderModes, mode)
              val result = helper.borderModeOfTransportYesNo
              result.get.key.value mustBe s"Do you want to add a border mode of transport?"
              result.get.value.value mustBe "No"
              val actions = result.get.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.AddBorderModeOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add a border mode of transport"
              action.id mustBe "change-add-border-mode"
          }
        }
      }
    }

    "borderModeOfTransport" - {
      "must return None" - {
        "when ModeCrossingBorderPage undefined" in {
          forAll(arbitrary[Mode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModes) =>
              val ie015WithNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoUserAnswers, departureId, borderModes, mode)
              val result = helper.borderModeOfTransport
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when ModeCrossingBorderPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[BorderMode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModeOfTransport, borderModes) =>
              val answers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, borderModes, mode)
              val result = helper.borderModeOfTransport

              result.get.key.value mustBe s"Mode"
              result.get.value.value mustBe borderModeOfTransport.description
              val actions = result.get.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "border mode of transport"
              action.id mustBe "change-border-mode-of-transport"
          }
        }

        "when ModeCrossingBorderPage defined in ie15" in {
          forAll(arbitrary[Mode], arbitrary[BorderMode], arbitrary[Seq[BorderMode]]) {
            (mode, borderModeOfTransport, borderModes) =>
              val answers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)
              val helper  = new PresentationNotificationAnswersHelper(answers, departureId, borderModes, mode)
              val result  = helper.borderModeOfTransport

              result.get.key.value mustBe s"Mode"
              result.get.value.value mustBe borderModeOfTransport.description
              val actions = result.get.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.transport.border.routes.BorderModeOfTransportController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "border mode of transport"
              action.id mustBe "change-border-mode-of-transport"
          }
        }
      }

    }

    "addUnlocodeYesNo" - {
      "must return None when no AddUnlocodeYesNo in ie15/170" - {
        s"when $AddUnLocodeYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithUnlocodeAddYesNoUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithUnlocodeAddYesNoUserAnswers, departureId, mode)
              val result = helper.addUnlocodeYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddUnLocodeYesNoPage defined in the ie170" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.addUnlocodeYesNo.get

              result.key.value mustBe s"Do you want to add a UN/LOCODE for the place of loading?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.AddUnLocodeYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want a UN/LOCODE for the place of loading"
              action.id mustBe "change-add-unlocode"
          }
        }

        s"when $AddUnLocodeYesNoPage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                      = new PresentationNotificationAnswersHelper(ie015WithLoadingUserAnswers, departureId, mode)
              val result                      = helper.addUnlocodeYesNo.get

              result.key.value mustBe s"Do you want to add a UN/LOCODE for the place of loading?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.AddUnLocodeYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want a UN/LOCODE for the place of loading"
              action.id mustBe "change-add-unlocode"
          }
        }
      }
    }

    "unlocode" - {
      "must return None when no unlocode in ie15/170" - {
        s"when $UnLocodePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithLoadingUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithLoadingUserAnswers, departureId, mode)
              val result = helper.unlocode
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $UnLocodePage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryUnLocode.arbitrary) {
            (mode, unlocode) =>
              val answers = emptyUserAnswers
                .setValue(UnLocodePage, unlocode)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.unlocode.get

              result.key.value mustBe s"UN/LOCODE"
              result.value.value mustBe unlocode
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.UnLocodeController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "UN/LOCODE for the place of loading"
              action.id mustBe "change-unlocode"
          }
        }

        s"when $UnLocodePage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                      = new PresentationNotificationAnswersHelper(ie015WithLoadingUserAnswers, departureId, mode)
              val result                      = helper.unlocode.get

              result.key.value mustBe s"UN/LOCODE"
              result.value.value mustBe messageData.Consignment.PlaceOfLoading.flatMap(_.UNLocode).get
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.UnLocodeController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "UN/LOCODE for the place of loading"
              action.id mustBe "change-unlocode"
          }
        }
      }
    }

    "addExtraInformationYesNo" - {
      "must return None when no addExtraInformation in ie15/170" - {
        s"when $AddExtraInformationYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoLoadingUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mode)
              val result = helper.addExtraInformationYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $AddExtraInformationYesNoPage defined in the ie170" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddExtraInformationYesNoPage, true)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.addExtraInformationYesNo.get

              result.key.value mustBe s"Do you want to add extra information for the place of loading?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.AddExtraInformationYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add extra information for the place of loading"
              action.id mustBe "change-add-extra-information"
          }
        }

        s"when $AddExtraInformationYesNoPage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                      = new PresentationNotificationAnswersHelper(ie015WithLoadingUserAnswers, departureId, mode)
              val result                      = helper.addExtraInformationYesNo.get

              result.key.value mustBe s"Do you want to add extra information for the place of loading?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.AddExtraInformationYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add extra information for the place of loading"
              action.id mustBe "change-add-extra-information"
          }
        }
      }
    }

    "country" - {
      "must return None when no country in ie15/170" - {
        s"when $CountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoLoadingUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mode)
              val result = helper.country
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CountryPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryCountry.arbitrary) {
            (mode, country) =>
              val answers = emptyUserAnswers
                .setValue(CountryPage, country)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.country.get

              result.key.value mustBe s"Country"
              result.value.value mustBe country.toString
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.CountryController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "country for the place of loading"
              action.id mustBe "change-country"
          }
        }

        s"when $CountryPage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithLoadingUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                      = new PresentationNotificationAnswersHelper(ie015WithLoadingUserAnswers, departureId, mode)
              val result                      = helper.country.get

              result.key.value mustBe s"Country"
              //TODO: Change once we pull ref data to format country answer in the 15
              result.value.value mustBe s"countryDesc - ${messageData.Consignment.PlaceOfLoading.flatMap(_.country).get}"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.CountryController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "country for the place of loading"
              action.id mustBe "change-country"
          }
        }
      }
    }

    "location" - {
      "must return None when no location in ie15/170" - {
        s"when $LocationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithNoLoadingUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])
              val helper = new PresentationNotificationAnswersHelper(ie015WithNoLoadingUserAnswers, departureId, mode)
              val result = helper.location
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when $LocationPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString.sample.value) {
            (mode, location) =>
              val answers = emptyUserAnswers
                .setValue(LocationPage, location)
              val helper = new PresentationNotificationAnswersHelper(answers, departureId, mode)
              val result = helper.location.get

              result.key.value mustBe s"Location"
              result.value.value mustBe location
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.LocationController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "location for the place of loading"
              action.id mustBe "change-location"
          }
        }

        s"when $LocationPage defined in the ie15" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithContainerIndicatorUserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)
              val helper                                 = new PresentationNotificationAnswersHelper(ie015WithContainerIndicatorUserAnswers, departureId, mode)
              val result                                 = helper.location.get

              result.key.value mustBe s"Location"
              result.value.value mustBe messageData.Consignment.PlaceOfLoading.flatMap(_.location).get
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.loading.routes.LocationController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "location for the place of loading"
              action.id mustBe "change-location"
          }
        }
      }
    }

  }
}
