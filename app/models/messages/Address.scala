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

package models.messages

import models.DynamicAddress
import models.reference.{Country, CountryCode}
import play.api.libs.json.{Json, OFormat}

case class Address(
  streetAndNumber: String,
  postcode: Option[String],
  city: String,
  country: String
) {

  def toCountry = Country(code = CountryCode(country), description = "")

  def toDynamicAddress: DynamicAddress = DynamicAddress(
    numberAndStreet = streetAndNumber,
    city = city,
    postalCode = postcode
  )
}

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}
