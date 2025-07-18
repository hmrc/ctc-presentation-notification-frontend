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

import forms.Constants.maxIdentificationNumberLength
import forms.behaviours.StringFieldBehaviours
import models.StringFieldRegex.alphaNumericRegex
import org.scalacheck.Gen
import play.api.data.FormError

class DepartureTransportMeansIdentificationNumberFormProviderSpec extends StringFieldBehaviours {

  private val prefix = Gen.alphaNumStr.sample.value

  private val requiredKey = s"$prefix.error.required"
  private val invalidKey  = s"$prefix.error.invalid"
  private val lengthKey   = s"$prefix.error.length"

  val form = new DepartureTransportMeansIdentificationNumberFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxIdentificationNumberLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxIdentificationNumberLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxIdentificationNumberLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.regex)),
      maxIdentificationNumberLength
    )

    "must convert input to upper case" in {
      val result = form.bind(Map(fieldName -> "abc123def456"))
      result.value.value mustEqual "ABC123DEF456"
    }
  }
}
