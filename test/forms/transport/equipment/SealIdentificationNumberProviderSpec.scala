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

package forms.transport.equipment

import forms.Constants.maxSealIdentificationLength
import forms.behaviours.StringFieldBehaviours
import models.StringFieldRegex.alphaNumericWithSpacesRegex
import org.scalacheck.Gen
import play.api.data.{Field, FormError}

class SealIdentificationNumberProviderSpec extends StringFieldBehaviours {

  private val prefix               = Gen.alphaNumStr.sample.value
  private val invalidCharactersKey = s"$prefix.error.invalid"
  private val requiredKey          = s"$prefix.error.required"
  private val maxLengthKey         = s"$prefix.error.length"
  val duplicateKey                 = s"$prefix.error.duplicate"
  private val maxLength            = 35

  val form = new SealIdentificationNumberFormProvider()(prefix, Seq.empty)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidCharactersKey, Seq(alphaNumericWithSpacesRegex.regex)),
      maxSealIdentificationLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind valid strings over max length" in {
      val expectedError = FormError(fieldName, maxLengthKey, Seq(maxSealIdentificationLength))

      val gen = for {
        str <- stringsLongerThan(maxSealIdentificationLength, Gen.alphaNumChar)
      } yield str

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind if value exists in the list of other ids" in {
      val otherIds  = Seq("foo", "bar")
      val form      = new SealIdentificationNumberFormProvider()(prefix, otherIds)
      val boundForm = form.bind(Map("value" -> "foo"))
      val field     = boundForm("value")
      field.errors mustEqual Seq(FormError(fieldName, duplicateKey))
    }
  }
}