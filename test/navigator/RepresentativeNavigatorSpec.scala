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

package navigator

import base.TestMessageData.representative
import base.{SpecBase, TestMessageData}
import generators.Generators
import models._
import models.messages.{Consignment, TransportEquipment}
import navigation.RepresentativeNavigator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ActingAsRepresentativePage
import pages.representative.{AddRepresentativeContactDetailsYesNoPage, EoriPage, NamePage, RepresentativePhoneNumberPage}

class RepresentativeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new RepresentativeNavigator

  "RepresentativeNavigator" - {

    "in CheckMode" - {
      val mode = CheckMode
      "In IE015/latest IE013" - {
        "when 'Are you acting as a representative?' = Yes + 'Do you want to add your details?' = Yes" - {
          "must go from ActingAsRepresentativePage to CYA page" in {
            val userAnswers = emptyUserAnswers
              .setValue(ActingAsRepresentativePage, arbitrary[Boolean].sample.value)
            navigator
              .nextPage(ActingAsRepresentativePage, userAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

          "must go from AddRepresentativeContactDetailsYesNoPage to CYA page" in {
            val userAnswers = emptyUserAnswers
              .setValue(AddRepresentativeContactDetailsYesNoPage, arbitrary[Boolean].sample.value)
            navigator
              .nextPage(AddRepresentativeContactDetailsYesNoPage, userAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

          "must go from Representative NamePage to CYA page" in {
            val userAnswers = emptyUserAnswers
              .setValue(NamePage, nonEmptyString.sample.value)
            navigator
              .nextPage(NamePage, userAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }

          "must go from RepresentativePhoneNumberPage to CYA page" in {
            val userAnswers = emptyUserAnswers
              .setValue(RepresentativePhoneNumberPage, nonEmptyString.sample.value)
            navigator
              .nextPage(RepresentativePhoneNumberPage, userAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "when 'Are you acting as a representative?' = Yes + 'Do you want to add your details?' = No" - {
          "must go from AddRepresentativeContactDetailsYesNoPage" - {
            "to CYA page if answer remains as 'No'" in {
              val userAnswers = emptyUserAnswers
                .setValue(AddRepresentativeContactDetailsYesNoPage, false)
              navigator
                .nextPage(AddRepresentativeContactDetailsYesNoPage, userAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
            }

            "to Representative NamePage if answer is changed to 'Yes'" in {
              val userAnswers = emptyUserAnswers
                .setValue(AddRepresentativeContactDetailsYesNoPage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Representative = Some(representative.copy(ContactPerson = None))
                  )
                )
              navigator
                .nextPage(AddRepresentativeContactDetailsYesNoPage, userAnswers, departureId, mode)
                .mustBe(NamePage.route(userAnswers, departureId, mode).value)
            }
          }

          "must go from NamePage to RepresentativePhoneNumberPage" in {
            val userAnswers = emptyUserAnswers
              .setValue(AddRepresentativeContactDetailsYesNoPage, true)
              .copy(departureData =
                TestMessageData.messageData.copy(
                  Representative = Some(representative.copy(ContactPerson = None))
                )
              )
              .setValue(NamePage, nonEmptyString.sample.value)
            navigator
              .nextPage(NamePage, userAnswers, departureId, mode)
              .mustBe(RepresentativePhoneNumberPage.route(userAnswers, departureId, mode).value)
          }

          "must go from RepresentativePhoneNumberPage to CYA page" in {
            val userAnswers = emptyUserAnswers
              .setValue(AddRepresentativeContactDetailsYesNoPage, true)
              .copy(departureData =
                TestMessageData.messageData.copy(
                  Representative = Some(representative.copy(ContactPerson = None))
                )
              )
              .setValue(NamePage, nonEmptyString.sample.value)
              .setValue(RepresentativePhoneNumberPage, nonEmptyString.sample.value)
            navigator
              .nextPage(RepresentativePhoneNumberPage, userAnswers, departureId, mode)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
          }
        }

        "when 'Are you acting as a representative?' = No" - {
          "must go from ActingAsRepresentative page" - {
            "to CYA page if answer remains as 'No'" in {
              val userAnswers = emptyUserAnswers
                .setValue(ActingAsRepresentativePage, false)
              navigator
                .nextPage(ActingAsRepresentativePage, userAnswers, departureId, mode)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
            }

            "to Representative EoriNumberPage if answer is changed to 'Yes'" in {
              val userAnswers = emptyUserAnswers
                .setValue(ActingAsRepresentativePage, true)
                .copy(departureData =
                  TestMessageData.messageData.copy(
                    Representative = None
                  )
                )
              navigator
                .nextPage(ActingAsRepresentativePage, userAnswers, departureId, mode)
                .mustBe(EoriPage.route(userAnswers, departureId, mode).value)
            }
          }

          "must go from EoriNumber page to AddRepresentativeContactDetailsPage" in {
            val userAnswers = emptyUserAnswers
              .setValue(ActingAsRepresentativePage, true)
              .setValue(EoriPage, nonEmptyString.sample.value)
            navigator
              .nextPage(EoriPage, userAnswers, departureId, mode)
              .mustBe(AddRepresentativeContactDetailsYesNoPage.route(userAnswers, departureId, mode).value)
          }
        }
      }
    }
  }

}
