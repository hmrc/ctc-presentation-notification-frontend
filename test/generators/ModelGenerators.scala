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

import models.*
import models.AddressLine.{City, NumberAndStreet, PostalCode}
import models.StringFieldRegex.{coordinatesLatitudeMaxRegex, coordinatesLongitudeMaxRegex}
import models.departureP5.MessageType
import models.departureP5.MessageType.*
import models.reference.*
import models.reference.TransportMode.*
import models.reference.transport.border.active
import models.reference.transport.transportMeans.TransportMeansIdentification
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
        locationType <- nonEmptyString
        description  <- nonEmptyString
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
        qualifier   <- nonEmptyString
        description <- nonEmptyString
      } yield LocationOfGoodsIdentification(qualifier, description)
    }

  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =
    Arbitrary {
      Gen
        .pick(2, 'A' to 'Z')
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

  implicit lazy val arbitraryUnLocode: Arbitrary[UnLocode] =
    Arbitrary {
      for {
        unLocodeExtendedCode <- stringsWithExactLength(5)
        name                 <- nonEmptyString
      } yield UnLocode(unLocodeExtendedCode, name)
    }

  implicit lazy val arbitraryNationality: Arbitrary[Nationality] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield Nationality(code, desc)
    }

  implicit lazy val arbitrarySpecificCircumstanceIndicator: Arbitrary[SpecificCircumstanceIndicator] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield SpecificCircumstanceIndicator(code, desc)
    }

  lazy val arbitrarySecurityDetailsNonZeroType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("1", "2", "3")
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

  implicit lazy val arbitraryItem: Arbitrary[Item] =
    Arbitrary {
      for {
        no   <- positiveInts
        desc <- nonEmptyString
      } yield Item(no, desc)
    }

  implicit lazy val arbitraryBorderModeOfTransport: Arbitrary[BorderMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4")
        description <- nonEmptyString
      } yield BorderMode(code, description)
    }

  implicit lazy val arbitraryInlandModeOfTransport: Arbitrary[InlandMode] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4")
        description <- nonEmptyString
      } yield InlandMode(code, description)
    }

  implicit lazy val arbitraryOptionalNonAirBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "7", "8", "9")
        description <- nonEmptyString
      } yield Some(BorderMode(code, description))
    }

  implicit lazy val arbitraryOptionalNonMailBorderModeOfTransport: Arbitrary[Option[BorderMode]] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("1", "2", "3", "4", "7", "8", "9")
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

  implicit lazy val arbitraryIdentificationActive: Arbitrary[active.Identification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield active.Identification(code, description)
    }

  implicit lazy val arbitraryTransportMeansIdentification: Arbitrary[TransportMeansIdentification] =
    Arbitrary {
      for {
        code        <- Gen.oneOf("10", "11", "21", "30", "40", "41", "80", "81")
        description <- nonEmptyString
      } yield TransportMeansIdentification(code, description)
    }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }

  implicit lazy val arbitraryMessageType: Arbitrary[MessageType] =
    Arbitrary {
      for {
        value <- nonEmptyString
        result <- Gen.oneOf(
          DeclarationData,
          DeclarationAmendment,
          PresentationForThePreLodgedDeclaration,
          PositiveAcknowledgement,
          AmendmentAcceptance,
          ControlDecisionNotification,
          Other(value)
        )
      } yield result
    }
}
