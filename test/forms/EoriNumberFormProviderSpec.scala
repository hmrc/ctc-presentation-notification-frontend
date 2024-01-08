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

package forms

import config.Constants.maxEoriNumberLength
import forms.behaviours.{FieldBehaviours, StringFieldBehaviours}
import models.StringFieldRegex.alphaNumericRegex
import org.scalacheck.Gen
import play.api.data.FormError

class EoriNumberFormProviderSpec extends StringFieldBehaviours with FieldBehaviours {

  private val prefix = Gen.alphaNumStr.sample.value

  private val requiredKey          = s"$prefix.error.required"
  private val maxLengthKey         = s"$prefix.error.length"
  private val invalidCharactersKey = s"$prefix.error.invalid"

  private val form = new EoriNumberFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = stringsWithMaxLength(maxEoriNumberLength)
    )

    behave like fieldThatRemovesSpaces(
      form,
      fieldName,
      stringsWithMaxLength(maxEoriNumberLength)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      error = FormError(fieldName, invalidCharactersKey, Seq(alphaNumericRegex.regex)),
      length = maxEoriNumberLength
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxEoriNumberLength,
      lengthError = FormError(fieldName, maxLengthKey, Seq(maxEoriNumberLength))
    )
  }
}
