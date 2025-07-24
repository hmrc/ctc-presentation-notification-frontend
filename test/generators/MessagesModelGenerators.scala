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

import generated.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar

import javax.xml.datatype.XMLGregorianCalendar

trait MessagesModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryMESSAGESequence: Arbitrary[MESSAGESequence] =
    Arbitrary {
      for {
        messageSender          <- Gen.alphaNumStr
        messageRecipient       <- Gen.alphaNumStr
        preparationDateAndTime <- arbitrary[XMLGregorianCalendar]
        messageIdentification  <- Gen.alphaNumStr
        correlationIdentifier  <- Gen.option(Gen.alphaNumStr)
      } yield MESSAGESequence(
        messageSender = messageSender,
        messageRecipient = messageRecipient,
        preparationDateAndTime = preparationDateAndTime,
        messageIdentification = messageIdentification,
        messageType = CC043C,
        correlationIdentifier = correlationIdentifier
      )
    }

  implicit lazy val arbitraryTransitOperationType10: Arbitrary[TransitOperationType10] =
    Arbitrary {
      for {
        mrn                       <- Gen.alphaNumStr
        declarationType           <- Gen.option(Gen.alphaNumStr)
        declarationAcceptanceDate <- Gen.option(arbitrary[XMLGregorianCalendar])
        security                  <- Gen.alphaNumStr
        reducedDatasetIndicator   <- arbitrary[Flag]
      } yield TransitOperationType10(
        MRN = mrn,
        declarationType = declarationType,
        declarationAcceptanceDate = declarationAcceptanceDate,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator
      )
    }

  implicit lazy val arbitraryTransitOperationType11: Arbitrary[TransitOperationType11] =
    Arbitrary {
      for {
        mrn                 <- Gen.alphaNumStr
        otherThingsToReport <- Gen.option(Gen.alphaNumStr)
      } yield TransitOperationType11(
        MRN = mrn,
        otherThingsToReport = otherThingsToReport
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationActualType03: Arbitrary[CustomsOfficeOfDestinationActualType03] =
    Arbitrary {
      for {
        referenceNumber <- Gen.alphaNumStr
      } yield CustomsOfficeOfDestinationActualType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType06: Arbitrary[HolderOfTheTransitProcedureType06] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(Gen.alphaNumStr)
        tirHolderIdentificationNumber <- Gen.option(Gen.alphaNumStr)
        name                          <- Gen.alphaNumStr
        address                       <- arbitrary[AddressType15]
      } yield HolderOfTheTransitProcedureType06(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryTraderAtDestinationType02: Arbitrary[TraderAtDestinationType02] =
    Arbitrary {
      for {
        identificationNumber <- Gen.alphaNumStr
      } yield TraderAtDestinationType02(
        identificationNumber = identificationNumber
      )
    }

  implicit lazy val arbitraryCTLControlType: Arbitrary[CTLControlType] =
    Arbitrary {
      for {
        continueUnloading <- positiveBigInts
      } yield CTLControlType(
        continueUnloading = continueUnloading
      )
    }

  implicit lazy val arbitraryConsignorType05: Arbitrary[ConsignorType05] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType14])
      } yield ConsignorType05(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsigneeType01: Arbitrary[ConsigneeType01] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType01])
      } yield ConsigneeType01(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsigneeType05: Arbitrary[ConsigneeType05] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType14])
      } yield ConsigneeType05(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsignorType04: Arbitrary[ConsignorType04] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType14])
      } yield ConsignorType04(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryDepartureTransportMeansType01: Arbitrary[DepartureTransportMeansType01] =
    Arbitrary {
      for {
        sequenceNumber       <- positiveBigInts
        typeOfIdentification <- Gen.alphaNumStr
        identificationNumber <- Gen.alphaNumStr
        nationality          <- Gen.alphaNumStr
      } yield DepartureTransportMeansType01(
        sequenceNumber = sequenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality
      )
    }

  implicit lazy val arbitraryTransportEquipmentType03: Arbitrary[TransportEquipmentType03] =
    Arbitrary {
      for {
        seqNum          <- Gen.posNum[Long].map(BigInt(_))
        containerId     <- Gen.alphaNumStr.suchThat(_.nonEmpty).map(Some(_)) // Always Some
        numberOfSeals   <- Gen.choose(1, 3).map(BigInt(_))
        seals           <- arbitrary[Seq[SealType01]]
        goodsReferences <- arbitrary[Seq[GoodsReferenceType01]]
      } yield TransportEquipmentType03(
        sequenceNumber = seqNum,
        containerIdentificationNumber = containerId,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitraryTransportEquipmentType06: Arbitrary[TransportEquipmentType06] =
    Arbitrary {
      for {
        sequenceNumber                <- positiveBigInts
        containerIdentificationNumber <- Gen.option(Gen.alphaNumStr)
        seals                         <- arbitrary[Seq[SealType01]]
        numberOfSeals                 <- Gen.option(positiveBigInts)
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType03]]
      } yield TransportEquipmentType06(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType01: Arbitrary[AdditionalReferenceType01] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        typeVal        <- Gen.alphaNumStr
        refNum         <- Gen.option(Gen.alphaNumStr)
      } yield AdditionalReferenceType01(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType02: Arbitrary[AdditionalReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        typeVal        <- Gen.alphaNumStr
        refNum         <- Gen.option(Gen.alphaNumStr)
      } yield AdditionalReferenceType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitraryAdditionalInformationType02: Arbitrary[AdditionalInformationType02] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        code           <- Gen.alphaNumStr
        text           <- Gen.option(Gen.alphaNumStr)
      } yield AdditionalInformationType02(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text
      )
    }

  implicit lazy val arbitrarySealType01: Arbitrary[SealType01] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        identifier     <- Gen.alphaNumStr
      } yield SealType01(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryPackageType04: Arbitrary[PackagingType01] =
    Arbitrary {
      for {
        sequenceNumber   <- positiveBigInts
        typeOfPackages   <- Gen.alphaNumStr
        numberOfPackages <- Gen.option(positiveBigInts)
        shippingMarks    <- Gen.option(Gen.alphaNumStr)

      } yield PackagingType01(
        sequenceNumber = sequenceNumber,
        typeOfPackages,
        numberOfPackages,
        shippingMarks
      )
    }

  implicit lazy val arbitraryGoodsReferenceType03: Arbitrary[GoodsReferenceType03] =
    Arbitrary {
      for {
        sequenceNumber             <- positiveBigInts
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType03(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryGoodsReferenceType01: Arbitrary[GoodsReferenceType01] =
    Arbitrary {
      for {
        sequenceNumber             <- positiveBigInts
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType01(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryCommodityCodeType05: Arbitrary[CommodityCodeType05] =
    Arbitrary {
      for {
        harmonizedSystemSubHeadingCode <- Gen.alphaNumStr
        combinedNomenclatureCode       <- Gen.option(Gen.alphaNumStr)
      } yield CommodityCodeType05(
        harmonizedSystemSubHeadingCode = harmonizedSystemSubHeadingCode,
        combinedNomenclatureCode = combinedNomenclatureCode
      )
    }

  implicit lazy val arbitraryDangerousGoodsType01: Arbitrary[DangerousGoodsType01] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        unNumber       <- Gen.alphaNumStr
      } yield DangerousGoodsType01(
        sequenceNumber = sequenceNumber,
        UNNumber = unNumber
      )
    }

  implicit lazy val arbitraryEndorsementType02: Arbitrary[EndorsementType02] = Arbitrary {
    for {
      date      <- arbitraryXMLGregorianCalendar.arbitrary
      authority <- Gen.alphaNumStr
      place     <- Gen.alphaNumStr
      country   <- Gen.alphaNumStr
    } yield EndorsementType02(date, authority, place, country)
  }

  implicit lazy val arbitraryTranshipmentType: Arbitrary[TranshipmentType] = Arbitrary {
    for {
      containerIndicator <- arbitraryFlag.arbitrary
      transportMeans     <- arbitrary[TransportMeansType]
    } yield TranshipmentType(containerIndicator, transportMeans)
  }

  implicit lazy val arbitraryTransportMeansType: Arbitrary[TransportMeansType] = Arbitrary {
    for {
      typeOfIdentification <- Gen.alphaNumStr
      identificationNumber <- Gen.alphaNumStr
      nationality          <- Gen.alphaNumStr
    } yield TransportMeansType(
      typeOfIdentification = typeOfIdentification,
      identificationNumber = identificationNumber,
      nationality = nationality
    )
  }

  implicit lazy val arbitraryIncidentType03: Arbitrary[generated.IncidentType03] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        code           <- Gen.alphaNumStr
        text           <- Gen.alphaNumStr
        loc            <- arbitraryLocationType.arbitrary

      } yield generated.IncidentType03(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text,
        Endorsement = None,
        Location = loc,
        TransportEquipment = Nil,
        Transhipment = None
      )
    }

  implicit lazy val arbitraryAddressType01: Arbitrary[AddressType01] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
        country         <- Gen.alphaNumStr
      } yield AddressType01(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType14: Arbitrary[AddressType14] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
        country         <- Gen.alphaNumStr
      } yield AddressType14(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType15: Arbitrary[AddressType15] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
        country         <- Gen.alphaNumStr
      } yield AddressType15(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType21: Arbitrary[AddressType21] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
      } yield AddressType21(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city
      )
    }

  implicit lazy val arbitrarySupportingDocumentType02: Arbitrary[SupportingDocumentType02] =
    Arbitrary {
      for {
        sequenceNumber          <- positiveBigInts
        typeValue               <- Gen.alphaNumStr
        referenceNumber         <- Gen.alphaNumStr
        complementOfInformation <- Gen.option(Gen.alphaNumStr)
      } yield SupportingDocumentType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryTransportDocumentType01: Arbitrary[TransportDocumentType01] =
    Arbitrary {
      for {
        sequenceNumber  <- positiveBigInts
        typeValue       <- Gen.alphaNumStr
        referenceNumber <- Gen.alphaNumStr
      } yield TransportDocumentType01(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryPreviousDocumentType03: Arbitrary[PreviousDocumentType03] =
    Arbitrary {
      for {
        sequenceNumber          <- positiveBigInts
        typeValue               <- Gen.alphaNumStr
        referenceNumber         <- Gen.alphaNumStr
        complementOfInformation <- Gen.option(Gen.alphaNumStr)
      } yield PreviousDocumentType03(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryPreviousDocumentType05: Arbitrary[PreviousDocumentType05] =
    Arbitrary {
      for {
        sequenceNumber          <- positiveBigInts
        typeValue               <- Gen.alphaNumStr
        referenceNumber         <- Gen.alphaNumStr
        complementOfInformation <- Gen.option(Gen.alphaNumStr)
      } yield PreviousDocumentType05(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryPreviousDocumentType06: Arbitrary[PreviousDocumentType06] =
    Arbitrary {
      for {
        sequenceNumber          <- positiveBigInts
        typeValue               <- Gen.alphaNumStr
        referenceNumber         <- Gen.alphaNumStr
        complementOfInformation <- Gen.option(Gen.alphaNumStr)
      } yield PreviousDocumentType06(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryLocationType: Arbitrary[LocationType] =
    Arbitrary {
      for {
        qualifierOfIdentification <- Gen.alphaNumStr
        unLocode                  <- Gen.option(Gen.alphaNumStr)
        country                   <- Gen.alphaNumStr
        gnss                      <- Gen.option(arbitrary[GNSSType])
        address                   <- Gen.option(arbitrary[AddressType21])
      } yield LocationType(
        qualifierOfIdentification = qualifierOfIdentification,
        UNLocode = unLocode,
        country = country,
        GNSS = gnss,
        Address = address
      )
    }

  implicit lazy val arbitraryGNSSType: Arbitrary[GNSSType] =
    Arbitrary {
      for {
        latitude  <- Gen.alphaNumStr
        longitude <- Gen.alphaNumStr
      } yield GNSSType(
        latitude = latitude,
        longitude = longitude
      )
    }

  implicit lazy val arbitraryCountryOfRoutingOfConsignmentType02: Arbitrary[CountryOfRoutingOfConsignmentType02] =
    Arbitrary {
      for {
        sequenceNumber <- positiveBigInts
        country        <- Gen.alphaNumStr
      } yield CountryOfRoutingOfConsignmentType02(
        sequenceNumber = sequenceNumber,
        country = country
      )
    }

  implicit lazy val arbitraryXMLGregorianCalendar: Arbitrary[XMLGregorianCalendar] =
    Arbitrary {
      Gen.const(XMLCalendar("2022-07-15"))
    }

  implicit lazy val arbitraryFlag: Arbitrary[Flag] =
    Arbitrary {
      Gen.oneOf(Number0, Number1)
    }

  implicit lazy val arbitraryUnloadingRemarkType: Arbitrary[UnloadingRemarkType] =
    Arbitrary {
      for {
        conform             <- arbitrary[Flag]
        unloadingCompletion <- arbitrary[Flag]
        unloadingDate       <- arbitrary[XMLGregorianCalendar]
        stateOfSeals        <- Gen.option(arbitrary[Flag])
        unloadingRemark     <- Gen.option(nonEmptyString)
      } yield UnloadingRemarkType(
        conform = conform,
        unloadingCompletion = unloadingCompletion,
        unloadingDate = unloadingDate,
        stateOfSeals = stateOfSeals,
        unloadingRemark = unloadingRemark
      )
    }
}
