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

package utils.transformer.locationOfGoods

import base.SpecBase
import generated._
import generators.Generators
import models.LocationOfGoodsIdentification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.locationOfGoods.{AddContactYesNoPage, IdentificationPage}

class AddContactYesNoTransformerTest extends SpecBase with Generators {
  val transformer = new AddContactYesNoTransformer()

  "AddContactYesNoTransformer" - {
    "must return AddContactYesNoPage Yes (true) when there is ContactPerson and identification type is not V" in {
      forAll(arbitrary[LocationOfGoodsType05], arbitrary[ContactPersonType06]) {
        (locationOfGoods, contactPerson) =>
          val userAnswers =
            setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(ContactPerson = Some(contactPerson)))
              )(emptyUserAnswers)
              .setValue(IdentificationPage, LocationOfGoodsIdentification("X", "description"))

          val result = transformer.transform.apply(userAnswers).futureValue

          result.get(AddContactYesNoPage).get mustBe true
      }
    }

    "must return AddContactYesNoPage No (false) when there is no ContactPerson and identification is not V" in {
      forAll(arbitrary[LocationOfGoodsType05]) {
        locationOfGoods =>
          val userAnswers = setLocationOfGoodsOnUserAnswersLens
            .replace(
              Option(locationOfGoods.copy(ContactPerson = None))
            )(emptyUserAnswers)
            .setValue(IdentificationPage, LocationOfGoodsIdentification("X", "EoriIdentifier"))

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddContactYesNoPage).get mustBe false
      }
    }

    "must return AddContactYesNoPage None when identification is V" in {
      forAll(arbitrary[LocationOfGoodsType05], Gen.option(arbitrary[ContactPersonType06])) {
        (locationOfGoods, person) =>
          val userAnswers =
            setLocationOfGoodsOnUserAnswersLens
              .replace(Option(locationOfGoods.copy(ContactPerson = person)))(emptyUserAnswers)
              .setValue(IdentificationPage, LocationOfGoodsIdentification("V", "CustomsOfficeIdentifier"))

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddContactYesNoPage) mustBe None
      }
    }
  }
}
