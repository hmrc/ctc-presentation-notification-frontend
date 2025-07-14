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
import models.reference.{CustomsOffice, Nationality}
import models.{Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active._

class ActiveBorderTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "ActiveBorderTransportMeansAnswersHelper" - {

    "addBorderMeansOfTransportYesNo" - {
      "must return No when BorderMeansOfTransport set false in ie170" - {
        s"when $AddBorderMeansOfTransportYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val updatedAnswers = emptyUserAnswers.setValue(AddBorderMeansOfTransportYesNoPage, false)
              val helper         = new ActiveBorderTransportMeansAnswersHelper(updatedAnswers, departureId, mode, activeIndex)
              val result         = helper.addBorderMeansOfTransportYesNo.get

              result.key.value mustEqual "Do you want to add identification for the border means of transport?"
              result.value.value mustEqual "No"

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add identification for the border means of transport"
              action.id mustEqual "change-add-identification-for-the-border-means-of-transport"
          }
        }
      }

      "must return Yes when BorderMeansOfTransport has been answered in ie170" in {
        forAll(arbitrary[Mode], arbitrary[UserAnswers]) {
          (mode, userAnswers) =>
            val updatedAnswers = userAnswers.setValue(AddBorderMeansOfTransportYesNoPage, true)
            val helper         = new ActiveBorderTransportMeansAnswersHelper(updatedAnswers, departureId, mode, activeIndex)
            val result         = helper.addBorderMeansOfTransportYesNo.get

            result.key.value mustEqual "Do you want to add identification for the border means of transport?"
            result.value.value mustEqual "Yes"

            val actions = result.actions.get.items
            actions.size mustEqual 1
            val action = actions.head
            action.content.value mustEqual "Change"
            action.href mustEqual controllers.transport.border.routes.AddBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode).url
            action.visuallyHiddenText.get mustEqual "if you want to add identification for the border means of transport"
            action.id mustEqual "change-add-identification-for-the-border-means-of-transport"
        }
      }
    }

    "identificationType" - {
      "must return None when no identification type in ie170" - {
        s"when $IdentificationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.identificationType
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitraryIdentificationActive.arbitrary) {
            (mode, identification) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationPage(activeIndex), identification)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

              helper.identificationType.map {
                result =>
                  result.key.value mustEqual s"Identification type"
                  result.value.value mustEqual identification.asString
                  val actions = result.actions.get.items
                  actions.size mustEqual 1
                  val action = actions.head
                  action.content.value mustEqual "Change"
                  action.href mustEqual controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, activeIndex).url
                  action.visuallyHiddenText.get mustEqual "identification type for the border means of transport"
                  action.id mustEqual "change-identification-1"
              }
          }
        }
      }
    }

    "identificationNumber" - {
      "must return None when no identification number in ie170" - {
        s"when $IdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.identificationNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $IdentificationNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers
                .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

              val result = helper.identificationNumber.get

              result.key.value mustEqual s"Identification"
              result.value.value mustEqual identificationNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.active.routes.IdentificationNumberController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustEqual "identification for the border means of transport"
              action.id mustEqual "change-identification-number-1"
          }
        }
      }
    }

    "nationality" - {
      "must return None when no nationality in ie170" - {
        s"when $NationalityPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.nationality
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $NationalityPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[Nationality]) {
            (mode, nationality) =>
              val answers = emptyUserAnswers
                .setValue(NationalityPage(activeIndex), nationality)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

              val result = helper.nationality.get

              result.key.value mustEqual s"Registered country"
              result.value.value mustEqual nationality.description
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.active.routes.NationalityController.onPageLoad(departureId, mode, activeIndex).url
              action.visuallyHiddenText.get mustEqual "registered country for the border means of transport"
              action.id mustEqual "change-nationality-1"
          }
        }
      }
    }

    "customs office" - {
      "must return None when no customs office in ie170" - {
        s"when $CustomsOfficeActiveBorderPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.customsOffice
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $CustomsOfficeActiveBorderPage defined in the ie170" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              val answers = emptyUserAnswers
                .setValue(CustomsOfficeActiveBorderPage(activeIndex), customsOffice)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

              val result = helper.customsOffice.get

              result.key.value mustEqual s"Customs office"
              result.value.value mustEqual customsOffice.toString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.active.routes.CustomsOfficeActiveBorderController
                .onPageLoad(departureId, mode, activeIndex)
                .url
              action.visuallyHiddenText.get mustEqual "customs office for the border means of transport"
              action.id mustEqual "change-customs-office-1"
          }
        }
      }
    }

    "conveyanceReferenceNumberYesNo" - {
      "IE170" - {
        "must return None when no conveyanceReferenceNumberYesNo" - {
          s"when $AddConveyanceReferenceYesNoPage undefined" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
                val result = helper.conveyanceReferenceNumberYesNo
                result must not be defined
            }
          }
        }

        "must return Some(Row)" - {
          s"when $AddConveyanceReferenceYesNoPage defined in the ie170" in {
            forAll(arbitrary[Mode]) {
              mode =>
                val answers = emptyUserAnswers
                  .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

                val result = helper.conveyanceReferenceNumberYesNo.get

                result.key.value mustEqual "Do you want to add a conveyance reference number?"
                result.value.value mustEqual "Yes"
                val actions = result.actions.get.items
                actions.size mustEqual 1
                val action = actions.head
                action.content.value mustEqual "Change"
                action.href mustEqual controllers.transport.border.active.routes.AddConveyanceReferenceYesNoController
                  .onPageLoad(departureId, mode, activeIndex)
                  .url
                action.visuallyHiddenText.get mustEqual "if you want to add a conveyance reference number"
                action.id mustEqual "change-add-conveyance-reference-number-1"
            }
          }
        }
      }
    }

    "conveyanceReferenceNumber" - {
      "must return None when no conveyanceReferenceNumber in ie170" - {
        s"when $ConveyanceReferenceNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, activeIndex)
              val result = helper.conveyanceReferenceNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $ConveyanceReferenceNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, conveyanceReferenceNumber) =>
              val answers = emptyUserAnswers
                .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceReferenceNumber)
              val helper = new ActiveBorderTransportMeansAnswersHelper(answers, departureId, mode, activeIndex)

              val result = helper.conveyanceReferenceNumber.get

              result.key.value mustEqual s"Conveyance reference number"
              result.value.value mustEqual conveyanceReferenceNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.border.active.routes.ConveyanceReferenceNumberController
                .onPageLoad(departureId, mode, activeIndex)
                .url
              action.visuallyHiddenText.get mustEqual "conveyance reference number for the border means of transport"
              action.id mustEqual "change-conveyance-reference-number-1"
          }
        }
      }
    }
  }
}
