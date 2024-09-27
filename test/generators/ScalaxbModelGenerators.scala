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

import generated._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar

import java.time.LocalDateTime
import javax.xml.datatype.XMLGregorianCalendar

trait ScalaxbModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryCC015CType: Arbitrary[CC015CType] =
    Arbitrary {
      for {
        messageSequence1                         <- arbitrary[MESSAGESequence]
        transitOperation                         <- arbitrary[TransitOperationType06]
        customsOfficeOfDeparture                 <- arbitrary[CustomsOfficeOfDepartureType03]
        customsOfficeOfDestinationDeclaredType01 <- arbitrary[CustomsOfficeOfDestinationDeclaredType01]
        holderOfTheTransitProcedure              <- arbitrary[HolderOfTheTransitProcedureType14]
        consignment                              <- arbitrary[ConsignmentType20]
      } yield CC015CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        Authorisation = Nil,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        CustomsOfficeOfDestinationDeclared = customsOfficeOfDestinationDeclaredType01,
        CustomsOfficeOfTransitDeclared = Nil,
        CustomsOfficeOfExitForTransitDeclared = Nil,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = None,
        Guarantee = Nil,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC013CType: Arbitrary[CC013CType] =
    Arbitrary {
      for {
        messageSequence1                         <- arbitrary[MESSAGESequence]
        transitOperation                         <- arbitrary[TransitOperationType04]
        customsOfficeOfDeparture                 <- arbitrary[CustomsOfficeOfDepartureType03]
        customsOfficeOfDestinationDeclaredType01 <- arbitrary[CustomsOfficeOfDestinationDeclaredType01]
        holderOfTheTransitProcedure              <- arbitrary[HolderOfTheTransitProcedureType14]
        consignment                              <- arbitrary[ConsignmentType20]
      } yield CC013CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        Authorisation = Nil,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        CustomsOfficeOfDestinationDeclared = customsOfficeOfDestinationDeclaredType01,
        CustomsOfficeOfTransitDeclared = Nil,
        CustomsOfficeOfExitForTransitDeclared = Nil,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = None,
        Guarantee = Nil,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC170CType: Arbitrary[CC170CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType24]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType19]
        consignment                 <- arbitrary[ConsignmentType08]
      } yield CC170CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = None,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryActiveBorderTransportMeansType02: Arbitrary[ActiveBorderTransportMeansType02] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
      } yield ActiveBorderTransportMeansType02(
        sequenceNumber = sequenceNumber,
        customsOfficeAtBorderReferenceNumber = None,
        typeOfIdentification = None,
        identificationNumber = None,
        nationality = None,
        conveyanceReferenceNumber = None
      )
    }

  implicit lazy val arbitraryConsignmentItemType03: Arbitrary[CUSTOM_ConsignmentItemType03] =
    Arbitrary {
      for {
        goodsItemNumber            <- arbitrary[BigInt]
        declarationGoodsItemNumber <- arbitrary[BigInt]
        commodity                  <- arbitrary[CUSTOM_CommodityType08]
      } yield CUSTOM_ConsignmentItemType03(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDispatch = None,
        countryOfDestination = None,
        referenceNumberUCR = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None
      )
    }

  implicit lazy val arbitraryConsignmentItemType04: Arbitrary[CUSTOM_ConsignmentItemType04] =
    Arbitrary {
      for {
        goodsItemNumber            <- arbitrary[BigInt]
        declarationGoodsItemNumber <- arbitrary[BigInt]
        commodity                  <- arbitrary[CUSTOM_CommodityType08]
      } yield CUSTOM_ConsignmentItemType04(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDestination = None,
        Consignee = None,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil
      )
    }

  implicit lazy val arbitraryCommodityType08: Arbitrary[CUSTOM_CommodityType08] =
    Arbitrary {
      for {
        descriptionOfGoods <- nonEmptyString
      } yield CUSTOM_CommodityType08(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = None,
        CommodityCode = None,
        DangerousGoods = Nil,
        GoodsMeasure = None
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType05: Arbitrary[HolderOfTheTransitProcedureType05] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
      } yield HolderOfTheTransitProcedureType05(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = None,
        ContactPerson = None
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType14: Arbitrary[HolderOfTheTransitProcedureType14] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
      } yield HolderOfTheTransitProcedureType14(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = None,
        ContactPerson = None
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType19: Arbitrary[HolderOfTheTransitProcedureType19] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
      } yield HolderOfTheTransitProcedureType19(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = None
      )
    }

  implicit lazy val arbitraryConsignmentType04: Arbitrary[CUSTOM_ConsignmentType04] =
    Arbitrary {
      for {
        containerIndicator <- arbitrary[Flag]
        grossMass          <- arbitrary[BigDecimal]
      } yield CUSTOM_ConsignmentType04(
        countryOfDispatch = None,
        countryOfDestination = None,
        containerIndicator = containerIndicator,
        inlandModeOfTransport = None,
        modeOfTransportAtTheBorder = None,
        grossMass = grossMass,
        referenceNumberUCR = None,
        Carrier = None,
        Consignor = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        TransportEquipment = Nil,
        LocationOfGoods = None,
        DepartureTransportMeans = Nil,
        CountryOfRoutingOfConsignment = Nil,
        ActiveBorderTransportMeans = Nil,
        PlaceOfLoading = None,
        PlaceOfUnloading = None,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryConsignmentType08: Arbitrary[ConsignmentType08] =
    Arbitrary {
      for {
        containerIndicator <- Gen.option(arbitrary[Flag])
        locationOfGoods    <- arbitrary[LocationOfGoodsType03]
      } yield ConsignmentType08(
        containerIndicator = containerIndicator,
        inlandModeOfTransport = None,
        modeOfTransportAtTheBorder = None,
        TransportEquipment = Nil,
        LocationOfGoods = locationOfGoods,
        DepartureTransportMeans = Nil,
        ActiveBorderTransportMeans = Nil,
        PlaceOfLoading = None,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryConsignmentType20: Arbitrary[ConsignmentType20] =
    Arbitrary {
      for {
        grossMass <- arbitrary[BigDecimal]
      } yield ConsignmentType20(
        countryOfDispatch = None,
        countryOfDestination = None,
        containerIndicator = None,
        inlandModeOfTransport = None,
        modeOfTransportAtTheBorder = None,
        grossMass = grossMass,
        referenceNumberUCR = None,
        Carrier = None,
        Consignor = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        TransportEquipment = Nil,
        LocationOfGoods = None,
        DepartureTransportMeans = Nil,
        CountryOfRoutingOfConsignment = Nil,
        ActiveBorderTransportMeans = Nil,
        PlaceOfLoading = None,
        PlaceOfUnloading = None,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryHouseConsignmentType10: Arbitrary[HouseConsignmentType10] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
        grossMass      <- positiveInts
      } yield HouseConsignmentType10(
        sequenceNumber = sequenceNumber,
        countryOfDispatch = None,
        grossMass = grossMass,
        referenceNumberUCR = None,
        Consignor = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        DepartureTransportMeans = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None,
        ConsignmentItem = Nil
      )
    }

  implicit lazy val arbitraryConsignmentItemType09: Arbitrary[ConsignmentItemType09] =
    Arbitrary {
      for {
        goodsItemNumber            <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveInts
        commodity                  <- arbitrary[CommodityType07]
      } yield ConsignmentItemType09(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDispatch = None,
        countryOfDestination = None,
        referenceNumberUCR = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None
      )
    }

  implicit lazy val arbitraryDepartureTransportMeansType03: Arbitrary[DepartureTransportMeansType03] =
    Arbitrary {
      for {
        sequenceNumber       <- arbitrary[BigInt]
        typeOfIdentification <- Gen.option(nonEmptyString)
        identificationNumber <- Gen.option(nonEmptyString)
        nationality          <- Gen.option(nonEmptyString)
      } yield DepartureTransportMeansType03(
        sequenceNumber = sequenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality
      )
    }

  implicit lazy val arbitraryCommodityType07: Arbitrary[CommodityType07] =
    Arbitrary {
      for {
        descriptionOfGoods <- nonEmptyString
      } yield CommodityType07(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = None,
        CommodityCode = None,
        DangerousGoods = Nil,
        GoodsMeasure = None
      )
    }

  implicit lazy val arbitraryTraderAtDestinationType03: Arbitrary[TraderAtDestinationType03] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
      } yield TraderAtDestinationType03(
        identificationNumber = identificationNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationDeclaredType01: Arbitrary[CustomsOfficeOfDestinationDeclaredType01] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDestinationDeclaredType01(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDepartureType03: Arbitrary[CustomsOfficeOfDepartureType03] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDepartureType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationActualType03: Arbitrary[CustomsOfficeOfDestinationActualType03] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDestinationActualType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfExitForTransitDeclaredType02: Arbitrary[CustomsOfficeOfExitForTransitDeclaredType02] =
    Arbitrary {
      for {
        sequenceNumber  <- arbitrary[BigInt]
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfExitForTransitDeclaredType02(
        sequenceNumber = sequenceNumber,
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfTransitDeclaredType04: Arbitrary[CustomsOfficeOfTransitDeclaredType04] =
    Arbitrary {
      for {
        sequenceNumber  <- arbitrary[BigInt]
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfTransitDeclaredType04(
        sequenceNumber = sequenceNumber,
        referenceNumber = referenceNumber,
        arrivalDateAndTimeEstimated = None
      )
    }

  implicit lazy val arbitraryLocationOfGoodsType03: Arbitrary[LocationOfGoodsType03] =
    Arbitrary {
      for {
        typeOfLocation            <- nonEmptyString
        qualifierOfIdentification <- nonEmptyString
      } yield LocationOfGoodsType03(
        typeOfLocation = typeOfLocation,
        qualifierOfIdentification = qualifierOfIdentification,
        authorisationNumber = None,
        additionalIdentifier = None,
        UNLocode = None,
        CustomsOffice = None,
        GNSS = None,
        EconomicOperator = None,
        Address = None,
        PostcodeAddress = None,
        ContactPerson = None
      )
    }

  implicit lazy val arbitraryLocationOfGoodsType05: Arbitrary[LocationOfGoodsType05] =
    Arbitrary {
      for {
        typeOfLocation            <- nonEmptyString
        qualifierOfIdentification <- nonEmptyString
      } yield LocationOfGoodsType05(
        typeOfLocation = typeOfLocation,
        qualifierOfIdentification = qualifierOfIdentification,
        authorisationNumber = None,
        additionalIdentifier = None,
        UNLocode = None,
        CustomsOffice = None,
        GNSS = None,
        EconomicOperator = None,
        Address = None,
        PostcodeAddress = None,
        ContactPerson = None
      )
    }

  implicit lazy val arbitraryPlaceOfLoadingType03: Arbitrary[PlaceOfLoadingType03] =
    Arbitrary {
      for {
        unLocode <- Gen.option(nonEmptyString)
        country  <- Gen.option(nonEmptyString)
        location <- Gen.option(nonEmptyString)
      } yield PlaceOfLoadingType03(
        UNLocode = unLocode,
        country = country,
        location = location
      )
    }

  implicit lazy val arbitraryTransitOperationType04: Arbitrary[TransitOperationType04] =
    Arbitrary {
      for {
        lrn                       <- Gen.option(nonEmptyString)
        mrn                       <- Gen.option(nonEmptyString)
        declarationType           <- nonEmptyString
        additionalDeclarationType <- nonEmptyString
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
        bindingItinerary          <- arbitrary[Flag]
        amendmentTypeFlag         <- arbitrary[Flag]
      } yield TransitOperationType04(
        LRN = lrn,
        MRN = mrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = None,
        presentationOfTheGoodsDateAndTime = None,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = None,
        communicationLanguageAtDeparture = None,
        bindingItinerary = bindingItinerary,
        amendmentTypeFlag = amendmentTypeFlag,
        limitDate = None
      )
    }

  implicit lazy val arbitraryTransitOperationType06: Arbitrary[TransitOperationType06] =
    Arbitrary {
      for {
        lrn                       <- nonEmptyString
        declarationType           <- nonEmptyString
        additionalDeclarationType <- nonEmptyString
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
        bindingItinerary          <- arbitrary[Flag]
      } yield TransitOperationType06(
        LRN = lrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = None,
        presentationOfTheGoodsDateAndTime = None,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = None,
        communicationLanguageAtDeparture = None,
        bindingItinerary = bindingItinerary,
        limitDate = None
      )
    }

  implicit lazy val arbitraryTransitOperationType12: Arbitrary[TransitOperationType12] =
    Arbitrary {
      for {
        lrn                       <- nonEmptyString
        mrn                       <- nonEmptyString
        declarationType           <- nonEmptyString
        additionalDeclarationType <- nonEmptyString
        declarationAcceptanceDate <- arbitrary[XMLGregorianCalendar]
        releaseDate               <- arbitrary[XMLGregorianCalendar]
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
        bindingItinerary          <- arbitrary[Flag]
      } yield TransitOperationType12(
        LRN = lrn,
        MRN = mrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = None,
        declarationAcceptanceDate = declarationAcceptanceDate,
        releaseDate = releaseDate,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = None,
        communicationLanguageAtDeparture = None,
        bindingItinerary = bindingItinerary
      )
    }

  implicit lazy val arbitraryTransitOperationType14: Arbitrary[TransitOperationType14] =
    Arbitrary {
      for {
        mrn                     <- nonEmptyString
        security                <- nonEmptyString
        reducedDatasetIndicator <- arbitrary[Flag]
      } yield TransitOperationType14(
        MRN = mrn,
        declarationType = None,
        declarationAcceptanceDate = None,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator
      )
    }

  implicit lazy val arbitraryTransitOperationType24: Arbitrary[TransitOperationType24] =
    Arbitrary {
      for {
        lrn       <- nonEmptyString
        limitDate <- Gen.option(arbitrary[XMLGregorianCalendar])
      } yield TransitOperationType24(
        LRN = lrn,
        limitDate = limitDate
      )
    }

  implicit lazy val arbitraryRepresentativeType05: Arbitrary[RepresentativeType05] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        status               <- nonEmptyString
        contactPerson        <- Gen.option(arbitrary[ContactPersonType05])
      } yield RepresentativeType05(
        identificationNumber = identificationNumber,
        status = status,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryTransportEquipmentType06: Arbitrary[TransportEquipmentType06] =
    Arbitrary {
      for {
        sequenceNumber                <- arbitrary[BigInt]
        containerIdentificationNumber <- Gen.option(nonEmptyString)
        numberOfSeals                 <- positiveInts
        seals                         <- arbitrary[Seq[SealType05]]
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType02]]
      } yield TransportEquipmentType06(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitrarySealType05: Arbitrary[SealType05] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
        identifier     <- nonEmptyString
      } yield SealType05(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryGoodsReferenceType02: Arbitrary[GoodsReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber             <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveInts
      } yield GoodsReferenceType02(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryFlag: Arbitrary[Flag] =
    Arbitrary {
      for {
        bool <- arbitrary[Boolean]
      } yield if (bool) Number1 else Number0
    }

  implicit lazy val arbitraryMESSAGESequence: Arbitrary[MESSAGESequence] =
    Arbitrary {
      for {
        messageSender          <- nonEmptyString
        messageRecipient       <- nonEmptyString
        preparationDateAndTime <- arbitrary[XMLGregorianCalendar]
        messageIdentification  <- nonEmptyString
        messageType            <- arbitrary[MessageTypes]
        correlationIdentifier  <- Gen.option(nonEmptyString)
      } yield MESSAGESequence(
        messageSender = messageSender,
        messageRecipient = messageRecipient,
        preparationDateAndTime = preparationDateAndTime,
        messageIdentification = messageIdentification,
        messageType = messageType,
        correlationIdentifier = correlationIdentifier
      )
    }

  implicit lazy val arbitraryMessageTypes: Arbitrary[MessageTypes] =
    Arbitrary {
      Gen.oneOf(MessageTypes.values)
    }

  implicit lazy val arbitraryXMLGregorianCalendar: Arbitrary[XMLGregorianCalendar] =
    Arbitrary {
      XMLCalendar(LocalDateTime.now().toString)
    }

  implicit lazy val arbitraryTransportChargesType: Arbitrary[TransportChargesType] =
    Arbitrary {
      for {
        methodOfPayment <- nonEmptyString
      } yield TransportChargesType(
        methodOfPayment = methodOfPayment
      )
    }

  implicit lazy val arbitraryAddressType14: Arbitrary[AddressType14] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType14(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType17: Arbitrary[AddressType17] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType17(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryPostcodeAddressType02: Arbitrary[PostcodeAddressType02] =
    Arbitrary {
      for {
        houseNumber <- Gen.option(nonEmptyString)
        postcode    <- nonEmptyString
        country     <- nonEmptyString
      } yield PostcodeAddressType02(
        houseNumber = houseNumber,
        postcode = postcode,
        country = country
      )
    }

  implicit lazy val arbitraryContactPersonType05: Arbitrary[ContactPersonType05] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType05(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryContactPersonType06: Arbitrary[ContactPersonType06] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType06(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryGNSSType: Arbitrary[GNSSType] =
    Arbitrary {
      for {
        latitude  <- nonEmptyString
        longitude <- nonEmptyString
      } yield GNSSType(
        latitude = latitude,
        longitude = longitude
      )
    }
}
