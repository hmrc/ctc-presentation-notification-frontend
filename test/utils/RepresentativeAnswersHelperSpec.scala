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
import base.TestMessageData.{allOptionsNoneJsonValue, messageData, representative}
import generators.Generators
import models.messages.MessageData
import models.{Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.representative.{EoriPage, NamePage, RepresentativePhoneNumberPage}
import play.api.libs.json.Json

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class RepresentativeAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RepresentativeAnswersHelper" - {

    "actingAsRepresentative" - {
      "must return No" - {
        s"when Representative is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithoutRepresentativeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeUserAnswers, departureId, mode)
              val result = helper.actingAsRepresentative.get

              result.key.value mustBe "Are you acting as a representative?"
              result.value.value mustBe "No"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.routes.ActingAsRepresentativeController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you are acting as a representative"
              action.id mustBe "change-acting-as-representative"
          }
        }
      }

      "must return Some(Row)" - {
        s"when Representative is defined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithRepresentativeAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)

              val helper = new RepresentativeAnswersHelper(ie015WithRepresentativeAnswers, departureId, mode)
              val result = helper.actingAsRepresentative.get

              result.key.value mustBe "Are you acting as a representative?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.routes.ActingAsRepresentativeController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you are acting as a representative"
              action.id mustBe "change-acting-as-representative"
          }
        }
      }
    }

    "eori" - {
      "must return None" - {
        s"when Representative is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithoutRepresentativeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeUserAnswers, departureId, mode)
              val result = helper.eori

              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when Representative is defined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithRepresentativeAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)

              val helper = new RepresentativeAnswersHelper(ie015WithRepresentativeAnswers, departureId, mode)
              val result = helper.eori.get

              result.key.value mustBe "EORI number or Trader Identification Number (TIN)"
              result.value.value mustBe messageData.Representative.map(_.identificationNumber).get
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.EoriController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "representative’s EORI number or Trader Identification Number (TIN)"
              action.id mustBe "change-representative-eori"
          }
        }
      }

      "must return Some(Row)" - {
        s"when Eori is answered in IE170" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, eori) =>
              val answers = emptyUserAnswers.setValue(EoriPage, eori)
              val helper  = new RepresentativeAnswersHelper(answers, departureId, mode)
              val result  = helper.eori.get

              result.key.value mustBe "EORI number or Trader Identification Number (TIN)"
              result.value.value mustBe eori
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.EoriController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "representative’s EORI number or Trader Identification Number (TIN)"
              action.id mustBe "change-representative-eori"
          }
        }
      }
    }

    "addRepresentativeContactDetails" - {
      "must return None" - {
        s"when Representative is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithoutRepresentativeUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), allOptionsNoneJsonValue.as[MessageData])

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeUserAnswers, departureId, mode)
              val result = helper.addRepresentativeContactDetails()

              result mustBe None
          }
        }
      }

      "must return No" - {
        s"when Representative Contact Details is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val dataWithoutRepresentativeContactDetails = messageData.copy(
                Representative = Some(representative.copy(ContactPerson = None))
              )
              val ie015WithoutRepresentativeContactDetailsUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), dataWithoutRepresentativeContactDetails)

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeContactDetailsUserAnswers, departureId, mode)
              val result = helper.addRepresentativeContactDetails().get

              result.key.value mustBe "Do you want to add your details?"
              result.value.value mustBe "No"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.AddRepresentativeContactDetailsYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add your details"
              action.id mustBe "change-add-contact-details"
          }
        }
      }

      "must return Some(Row)" - {
        s"when Representative is defined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val ie015WithRepresentativeAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)

              val helper = new RepresentativeAnswersHelper(ie015WithRepresentativeAnswers, departureId, mode)
              val result = helper.addRepresentativeContactDetails().get

              result.key.value mustBe "Do you want to add your details?"
              result.value.value mustBe "Yes"
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.AddRepresentativeContactDetailsYesNoController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "if you want to add your details"
              action.id mustBe "change-add-contact-details"
          }
        }
      }
    }

    "name" - {
      "must return None" - {
        s"when Representative Contact is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val dataWithoutRepresentativeContactDetails = messageData.copy(
                Representative = Some(representative.copy(ContactPerson = None))
              )
              val ie015WithoutRepresentativeContactDetailsUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), dataWithoutRepresentativeContactDetails)

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeContactDetailsUserAnswers, departureId, mode)
              val result = helper.name

              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when Representative Contact name is answered" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, name) =>
              val answers = emptyUserAnswers.setValue(NamePage, name)
              val helper  = new RepresentativeAnswersHelper(answers, departureId, mode)
              val result  = helper.name.get

              result.key.value mustBe "Name"
              result.value.value mustBe name
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.NameController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "representative’s name"
              action.id mustBe "change-representative-name"
          }
        }
      }
    }

    "phoneNumber" - {
      "must return None" - {
        s"when Representative Contact is undefined in departure data IE015/013" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val dataWithoutRepresentativeContactDetails = messageData.copy(
                Representative = Some(representative.copy(ContactPerson = None))
              )
              val ie015WithoutRepresentativeContactDetailsUserAnswers =
                UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), dataWithoutRepresentativeContactDetails)

              val helper = new RepresentativeAnswersHelper(ie015WithoutRepresentativeContactDetailsUserAnswers, departureId, mode)
              val result = helper.phoneNumber

              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        s"when Representative Contact is answered" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, phoneNumber) =>
              val answers = emptyUserAnswers.setValue(RepresentativePhoneNumberPage, phoneNumber)
              val helper  = new RepresentativeAnswersHelper(answers, departureId, mode)
              val result  = helper.phoneNumber.get

              result.key.value mustBe "Phone number"
              result.value.value mustBe phoneNumber
              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe controllers.representative.routes.RepresentativePhoneNumberController.onPageLoad(departureId, mode).url
              action.visuallyHiddenText.get mustBe "representative’s phone number"
              action.id mustBe "change-representative-phone-number"
          }
        }
      }
    }
  }
}
