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

import com.google.inject.Inject
import forms.Constants.loadingLocationMaxLength
import forms.StopOnFirstFail
import forms.mappings.Mappings
import models.StringFieldRegex.stringFieldRegex
import play.api.data.Form

class LoadingLocationFormProvider @Inject() extends Mappings {

  def apply(prefix: String, args: String*): Form[String] =
    Form(
      "value" -> text(s"$prefix.error.required", args)
        .verifying(
          StopOnFirstFail[String](
            maxLength(loadingLocationMaxLength, s"$prefix.error.length", Seq(loadingLocationMaxLength)),
            regexp(stringFieldRegex, s"$prefix.error.invalid")
          )
        )
    )
}
