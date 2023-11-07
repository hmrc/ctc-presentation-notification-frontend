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

package generators

import models.AddressLine.{City, NumberAndStreet, PostalCode, StreetNumber}
import models.StringFieldRegex.{coordinatesLatitudeMaxRegex, coordinatesLongitudeMaxRegex}
import models._
import models.messages.ActiveBorderTransportMeans
import models.reference._
import models.reference.transport.border.active
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.{GET, POST}
import wolfendale.scalacheck.regexp.RegexpGen

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryMode: Arbitrary[Mode] = Arbitrary {
    Gen.oneOf(NormalMode, CheckMode)
  }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(17)
      } yield EoriNumber(number)
    }

  implicit lazy val arbitraryCoordinates: Arbitrary[Coordinates] =
    Arbitrary {
      for {
        latitude  <- RegexpGen.from(coordinatesLatitudeMaxRegex)
        longitude <- RegexpGen.from(coordinatesLongitudeMaxRegex)
      } yield Coordinates(latitude, longitude)
    }

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- stringsWithMaxLength(22: Int, Gen.alphaNumChar)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryLocationType: Arbitrary[LocationType] =
    Arbitrary {
      for {
        locationType <- Gen.alphaNumStr
        description  <- Gen.alphaNumStr
      } yield LocationType(locationType, description)
    }

  implicit def arbitrarySelectableList[T <: Selectable](implicit arbitrary: Arbitrary[T]): Arbitrary[SelectableList[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield SelectableList(values.distinctBy(_.value))
  }

  implicit lazy val arbitraryLocationOfGoodsIdentification: Arbitrary[LocationOfGoodsIdentification] =
    Arbitrary {
      for {
        qualifier   <- Gen.alphaNumStr
        description <- Gen.alphaNumStr
      } yield LocationOfGoodsIdentification(qualifier, description)
    }

  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =
    Arbitrary {
      Gen
        .pick(CountryCode.Constants.countryCodeLength, 'A' to 'Z')
        .map(
          code => CountryCode(code.mkString)
        )
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- arbitrary[CountryCode]
        name <- nonEmptyString
      } yield Country(code, name)
    }

  implicit lazy val arbitraryUnLocode: Arbitrary[String] =
    Arbitrary {
      for {
        id <- nonEmptyString
      } yield id
    }

  implicit lazy val arbitraryPostalCodeAddress: Arbitrary[PostalCodeAddress] =
    Arbitrary {
      for {
        streetNumber <- stringsWithMaxLength(StreetNumber.length, Gen.alphaNumChar)
        postalCode   <- stringsWithMaxLength(PostalCode.length, Gen.alphaNumChar)
        country      <- arbitrary[Country]
      } yield PostalCodeAddress(streetNumber, postalCode, country)
    }

  implicit lazy val arbitraryNationality: Arbitrary[Nationality] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield Nationality(code, desc)
    }

  lazy val arbitraryDynamicAddressWithRequiredPostalCode: Arbitrary[DynamicAddress] =
    Arbitrary {
      for {
        numberAndStreet <- stringsWithMaxLength(NumberAndStreet.length, Gen.alphaNumChar)
        city            <- stringsWithMaxLength(City.length, Gen.alphaNumChar)
        postalCode      <- stringsWithMaxLength(PostalCode.length, Gen.alphaNumChar)
      } yield DynamicAddress(numberAndStreet, city, Some(postalCode))
    }

  implicit lazy val arbitraryDynamicAddress: Arbitrary[DynamicAddress] =
    Arbitrary {
      for {
        numberAndStreet <- stringsWithMaxLength(NumberAndStreet.length, Gen.alphaNumChar)
        city            <- stringsWithMaxLength(City.length, Gen.alphaNumChar)
        postalCode      <- Gen.option(stringsWithMaxLength(PostalCode.length, Gen.alphaNumChar))
      } yield DynamicAddress(numberAndStreet, city, postalCode)
    }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- nonEmptyString
        name        <- nonEmptyString
        phoneNumber <- Gen.option(Gen.alphaNumStr)
      } yield CustomsOffice(id, name, phoneNumber)
    }

  implicit lazy val arbitraryBorderModeOfTransport: Arbitrary[BorderMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4")
        description <- nonEmptyString
      } yield BorderMode(code, description)
    }

  implicit lazy val arbitraryOptionalNonAirBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3")
        description <- nonEmptyString
      } yield Some(BorderMode(code, description))
    }

  lazy val arbitraryOptionalNonRailBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "3", "4")
        description <- nonEmptyString
      } yield Some(BorderMode(code, description))
    }

  lazy val arbitraryActiveBorderTransportMeans: Arbitrary[Option[List[ActiveBorderTransportMeans]]] =
    Arbitrary {
      for {
        sequenceNumber                       <- nonEmptyString
        customsOfficeAtBorderReferenceNumber <- Gen.option(nonEmptyString)
        typeOfIdentification                 <- Gen.option(nonEmptyString)
        identificationNumber                 <- Gen.option(nonEmptyString)
        nationality                          <- Gen.option(nonEmptyString)
        conveyanceReferenceNumber            <- Gen.option(nonEmptyString)
      } yield Some(
        List(
          ActiveBorderTransportMeans(sequenceNumber,
                                     customsOfficeAtBorderReferenceNumber,
                                     typeOfIdentification,
                                     identificationNumber,
                                     nationality,
                                     conveyanceReferenceNumber
          )
        )
      )
    }

  implicit lazy val arbitraryIdentificationActive: Arbitrary[active.Identification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield active.Identification(code, description)
    }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }
}
