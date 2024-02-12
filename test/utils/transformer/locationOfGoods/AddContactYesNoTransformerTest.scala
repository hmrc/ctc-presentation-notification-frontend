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
import models.UserAnswers
import pages.locationOfGoods.AddContactYesNoPage

class AddContactYesNoTransformerTest extends SpecBase {
  val transformer = new AddContactYesNoTransformer()

  "AddContactYesNoTransformer" - {
    "must return AddContactYesNoPage Yes (true) when there is ContactPerson" in {
      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(Option(locationOfGoods))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddContactYesNoPage).get mustBe true
      }
    }

    "must return AddContactYesNoPage No (false) when there is no ContactPerson" in {
      val userAnswers = UserAnswers.setLocationOfGoodsOnUserAnswersLens.set(Option(locationOfGoods.copy(ContactPerson = None)))(emptyUserAnswers)
      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(AddContactYesNoPage).get mustBe false
      }
    }
  }
}
