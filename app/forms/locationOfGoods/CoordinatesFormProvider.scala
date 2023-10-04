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

package forms.locationOfGoods

import forms.StopOnFirstFail
import forms.mappings.Mappings
import models.Coordinates
import models.StringFieldRegex._
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class CoordinatesFormProvider @Inject() extends Mappings {

  def apply(prefix: String): Form[Coordinates] =
    Form(
      mapping(
        "latitude" -> {
          lazy val args = Seq("latitude")
          text(s"$prefix.error.required", args)
            .verifying(
              StopOnFirstFail[String](
                regexp(coordinatesCharacterRegex, s"$prefix.error.invalid", args),
                regexp(coordinateFormatRegex, s"$prefix.error.format", args),
                regexp(coordinatesLatitudeMaxRegex.r, s"$prefix.error.latitude.maximum", args)
              )
            )
        },
        "longitude" -> {
          lazy val args = Seq("longitude")
          text(s"$prefix.error.required", args)
            .verifying(
              StopOnFirstFail[String](
                regexp(coordinatesCharacterRegex, s"$prefix.error.invalid", args),
                regexp(coordinateFormatRegex, s"$prefix.error.format", args),
                regexp(coordinatesLongitudeMaxRegex.r, s"$prefix.error.longitude.maximum", args)
              )
            )
        }
      )(Coordinates.apply)(Coordinates.unapply)
    )
}
