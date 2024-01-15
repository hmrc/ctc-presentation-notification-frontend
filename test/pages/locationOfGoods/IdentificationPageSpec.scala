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

package pages.locationOfGoods

import models.LocationOfGoodsIdentification
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.sections.locationOfGoods.QualifierOfIdentificationDetailsSection
import play.api.libs.json.Json

class IdentificationPageSpec extends PageBehaviours {

  "IdentificationPage" - {

    beRetrievable[LocationOfGoodsIdentification](IdentificationPage)

    beSettable[LocationOfGoodsIdentification](IdentificationPage)

    beRemovable[LocationOfGoodsIdentification](IdentificationPage)

    "cleanup" - {
      "must clean up LocationOfGoodsIdentifierSection and inferred value" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredIdentificationPage, qualifierOfIdentification)
              .setValue(QualifierOfIdentificationDetailsSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(InferredIdentificationPage) must not be defined
            result.get(QualifierOfIdentificationDetailsSection) must not be defined
        }
      }

      "must clean up AddIdentifierYesNoPage" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(AddIdentifierYesNoPage, true)

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(AddIdentifierYesNoPage) must not be defined
        }
      }

      "must clean up AddContactYesNoPage" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(AddContactYesNoPage, true)

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(AddContactYesNoPage) must not be defined
        }
      }

      "must clean up NamePage" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(NamePage, "name")

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(NamePage) must not be defined
        }
      }

      "must clean up PhoneNumberPage" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(PhoneNumberPage, "080000166")

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(PhoneNumberPage) must not be defined
        }
      }
    }
  }
}

class InferredIdentificationPageSpec extends PageBehaviours {

  "InferredIdentificationPage" - {

    beRetrievable[LocationOfGoodsIdentification](InferredIdentificationPage)

    beSettable[LocationOfGoodsIdentification](InferredIdentificationPage)

    beRemovable[LocationOfGoodsIdentification](InferredIdentificationPage)

    "cleanup" - {
      "must clean up LocationOfGoodsIdentifierSection and non-inferred value" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage, qualifierOfIdentification)
              .setValue(QualifierOfIdentificationDetailsSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(InferredIdentificationPage, qualifierOfIdentification)

            result.get(InferredIdentificationPage) mustBe defined
            result.get(IdentificationPage) must not be defined
            result.get(QualifierOfIdentificationDetailsSection) must not be defined
        }
      }
    }
  }
}
