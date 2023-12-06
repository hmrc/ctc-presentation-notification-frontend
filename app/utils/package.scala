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

import models.reference.transport.border.active.Identification
import models.reference._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

package object utils {

  implicit class EnrichedString(value: String) {

    def asBoolean: Boolean = value match {
      case "0" => false
      case "1" => true
      case x   => throw new IllegalArgumentException(s"could not cast $x to boolean")
    }

    def asBorderMode: BorderMode = BorderMode.getDescription(value)

    def asLocalDate: LocalDate = {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

      LocalDate.parse(value, formatter)
    }

    //TODO: Fetch this from ref data instead
    def asCountry: Country =
      Country(CountryCode(value), "countryDesc")

    def asIdentification: Identification = Identification(value, "")

    def asNationality: Nationality = Nationality(value, "")

    def asCustomsOffice: CustomsOffice = CustomsOffice(value, "", None)

  }

  implicit class RichLocalDate(localDate: LocalDate) {
    def formatAsString: String = localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

    def formatForText: String = localDate.format(DateTimeFormatter.ofPattern("dd MM yyyy"))
  }

}
