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

package models

import scala.util.matching.Regex

object StringFieldRegex {

  val coordinatesCharacterRegex: Regex     = "^[0-9.+-]+$".r
  val coordinatesLatitudeMaxRegex: String  = "^[+-]?([0-8]?[0-9]\\.[0-9]{5,7})$"
  val coordinateFormatRegex: Regex         = "^[+-]?([0-9]+\\.[0-9]{5,7})$".r
  val coordinatesLongitudeMaxRegex: String = "^[+-]?([0-1]?[0-7]?[0-9]\\.[0-9]{5,7})$"
  val alphaNumericRegex: Regex             = "^[a-zA-Z0-9]*$".r
  val stringFieldRegex: Regex              = "[\\sa-zA-Z0-9&'@/.\\-? ]*".r
  val alphaNumericWithSpacesRegex: Regex   = "^[a-zA-Z\\s0-9]*$".r
}