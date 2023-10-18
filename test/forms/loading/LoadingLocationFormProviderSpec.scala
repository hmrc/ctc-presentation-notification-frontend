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

package forms.loading

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.Constants.loadingLocationMaxLength
import forms.behaviours.StringFieldBehaviours
import models.StringFieldRegex.stringFieldRegex
import org.scalacheck.Gen
import play.api.data.{Field, FormError}

class LoadingLocationFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix = Gen.alphaNumStr.sample.value
  val requiredKey    = s"$prefix.error.required"
  val lengthKey      = s"$prefix.error.length"
  val invalidKey     = s"$prefix.error.invalid"

  val form = new LoadingLocationFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(loadingLocationMaxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(stringFieldRegex.regex)),
      loadingLocationMaxLength
    )

    "must not bind valid strings over max length" in {
      val expectedError = FormError(fieldName, lengthKey, Seq(loadingLocationMaxLength))

      val gen = for {
        str <- stringsLongerThan(loadingLocationMaxLength, Gen.alphaNumChar)
      } yield str

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

  }
}
