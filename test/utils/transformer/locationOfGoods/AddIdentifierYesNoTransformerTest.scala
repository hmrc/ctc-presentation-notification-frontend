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
import base.TestMessageData.locationOfGoods
import models.{LocationOfGoodsIdentification, UserAnswers}
import org.scalacheck.Gen
import pages.locationOfGoods.{AddIdentifierYesNoPage, IdentificationPage}

class AddIdentifierYesNoTransformerTest extends SpecBase {
  val transformer = new AddIdentifierYesNoTransformer()

  "AddIdentifierYesNoTransformer" - {
    "must return AddIdentifierYesNoPage Yes (true) when there is additionalIdentifier and identification is X or Y" in {
      forAll(Gen.oneOf("X", "Y")) {
        identification =>
          val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens
            .set(
              Option(locationOfGoods)
            )(emptyUserAnswers)
            .setValue(IdentificationPage, LocationOfGoodsIdentification(identification, "description"))

          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers.get(AddIdentifierYesNoPage).get mustBe true
          }
      }
    }

    "must return AddIdentifierYesNoPage No (false) when there is no additionalIdentifier and identification is X or Y" in {
      forAll(Gen.oneOf("X", "Y")) {
        identification =>
          val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens
            .set(
              Option(locationOfGoods.copy(additionalIdentifier = None))
            )(emptyUserAnswers)
            .setValue(IdentificationPage, LocationOfGoodsIdentification(identification, "description"))

          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers.get(AddIdentifierYesNoPage).get mustBe false
          }
      }
    }

    "must return AddIdentifierYesNoPage None when identification is not X or Y" in {
      forAll(Gen.oneOf(None, Some("additionalIdentifier"))) {
        additionalIdentifier =>
          val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens
            .set(
              Option(locationOfGoods.copy(additionalIdentifier = additionalIdentifier))
            )(emptyUserAnswers)
            .setValue(IdentificationPage, LocationOfGoodsIdentification("V", "description"))

          whenReady(transformer.transform(hc)(userAnswers)) {
            updatedUserAnswers =>
              updatedUserAnswers.get(AddIdentifierYesNoPage) mustBe None
          }
      }
    }
  }
}
