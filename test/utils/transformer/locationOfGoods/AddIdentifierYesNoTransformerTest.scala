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
import pages.locationOfGoods.{AddIdentifierYesNoPage, IdentificationPage, InferredIdentificationPage}

class AddIdentifierYesNoTransformerTest extends SpecBase with Generators {
  val transformer = new AddIdentifierYesNoTransformer()

  "AddIdentifierYesNoTransformer" - {
    "must return AddIdentifierYesNoPage Yes (true) when there is additionalIdentifier" - {
      "and identification is X or Y" in {
        forAll(arbitrary[LocationOfGoodsType05], nonEmptyString, Gen.oneOf("X", "Y")) {
          (locationOfGoods, additionalIdentifier, identification) =>
            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(additionalIdentifier = Some(additionalIdentifier)))
              )(emptyUserAnswers)
              .setValue(IdentificationPage, LocationOfGoodsIdentification(identification, "description"))

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(AddIdentifierYesNoPage).get mustBe true
        }
      }

      "and inferred identification is X or Y" in {
        forAll(arbitrary[LocationOfGoodsType05], nonEmptyString, Gen.oneOf("X", "Y")) {
          (locationOfGoods, additionalIdentifier, identification) =>
            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(additionalIdentifier = Some(additionalIdentifier)))
              )(emptyUserAnswers)
              .setValue(InferredIdentificationPage, LocationOfGoodsIdentification(identification, "description"))

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(AddIdentifierYesNoPage).get mustBe true
        }
      }
    }

    "must return AddIdentifierYesNoPage No (false) when there is no additionalIdentifier" - {
      "and identification is X or Y" in {
        forAll(arbitrary[LocationOfGoodsType05], Gen.oneOf("X", "Y")) {
          (locationOfGoods, identification) =>
            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(additionalIdentifier = None))
              )(emptyUserAnswers)
              .setValue(IdentificationPage, LocationOfGoodsIdentification(identification, "description"))

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(AddIdentifierYesNoPage).get mustBe false
        }
      }

      "and inferred identification is X or Y" in {
        forAll(arbitrary[LocationOfGoodsType05], Gen.oneOf("X", "Y")) {
          (locationOfGoods, identification) =>
            val userAnswers = setLocationOfGoodsOnUserAnswersLens
              .replace(
                Some(locationOfGoods.copy(additionalIdentifier = None))
              )(emptyUserAnswers)
              .setValue(InferredIdentificationPage, LocationOfGoodsIdentification(identification, "description"))

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(AddIdentifierYesNoPage).get mustBe false
        }
      }
    }

    "must return AddIdentifierYesNoPage None when identification is not X or Y" in {
      forAll(arbitrary[LocationOfGoodsType05], Gen.option(nonEmptyString)) {
        (locationOfGoods, additionalIdentifier) =>
          val userAnswers = setLocationOfGoodsOnUserAnswersLens
            .replace(
              Some(locationOfGoods.copy(additionalIdentifier = additionalIdentifier))
            )(emptyUserAnswers)
            .setValue(IdentificationPage, LocationOfGoodsIdentification("V", "description"))

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(AddIdentifierYesNoPage) mustBe None
      }
    }
  }
}
