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
        transitOperation                         <- arbitrary[TransitOperationType03]
        customsOfficeOfDeparture                 <- arbitrary[CustomsOfficeOfDepartureType05]
        customsOfficeOfDestinationDeclaredType01 <- arbitrary[CustomsOfficeOfDestinationDeclaredType01]
        holderOfTheTransitProcedure              <- arbitrary[HolderOfTheTransitProcedureType23]
        consignment                              <- arbitrary[ConsignmentType23]
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
        transitOperation                         <- arbitrary[TransitOperationType02]
        customsOfficeOfDeparture                 <- arbitrary[CustomsOfficeOfDepartureType05]
        customsOfficeOfDestinationDeclaredType01 <- arbitrary[CustomsOfficeOfDestinationDeclaredType01]
        holderOfTheTransitProcedure              <- arbitrary[HolderOfTheTransitProcedureType23]
        consignment                              <- arbitrary[ConsignmentType23]
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
        transitOperation            <- arbitrary[TransitOperationType23]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType13]
        consignment                 <- arbitrary[ConsignmentType10]
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

  implicit lazy val arbitraryActiveBorderTransportMeansType03: Arbitrary[ActiveBorderTransportMeansType03] =
    Arbitrary {
      for {
        sequenceNumber                       <- arbitrary[BigInt]
        customsOfficeAtBorderReferenceNumber <- nonEmptyString
        typeOfIdentification                 <- nonEmptyString
        identificationNumber                 <- nonEmptyString
        nationality                          <- nonEmptyString
      } yield ActiveBorderTransportMeansType03(
        sequenceNumber = sequenceNumber,
        customsOfficeAtBorderReferenceNumber = customsOfficeAtBorderReferenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality,
        conveyanceReferenceNumber = None
      )
    }

  /*implicit lazy val arbitraryConsignmentItemType03: Arbitrary[CUSTOM_ConsignmentItemType03] =
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
    }*/

  implicit lazy val arbitraryHolderOfTheTransitProcedureType23: Arbitrary[HolderOfTheTransitProcedureType23] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
      } yield HolderOfTheTransitProcedureType23(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = None,
        ContactPerson = None
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType13: Arbitrary[HolderOfTheTransitProcedureType13] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
      } yield HolderOfTheTransitProcedureType13(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = None
      )
    }

  /*implicit lazy val arbitraryConsignmentType04: Arbitrary[CUSTOM_ConsignmentType04] =
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
    }*/

  implicit lazy val arbitraryConsignmentType10: Arbitrary[ConsignmentType10] =
    Arbitrary {
      for {
        containerIndicator <- Gen.option(arbitrary[Flag])
        locationOfGoods    <- arbitrary[LocationOfGoodsType02]
      } yield ConsignmentType10(
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

  implicit lazy val arbitraryConsignmentType23: Arbitrary[ConsignmentType23] =
    Arbitrary {
      for {
        grossMass <- arbitrary[BigDecimal]
      } yield ConsignmentType23(
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

  implicit lazy val arbitraryHouseConsignmentType13: Arbitrary[HouseConsignmentType13] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
        grossMass      <- positiveInts
      } yield HouseConsignmentType13(
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

  implicit lazy val arbitraryConsignmentItemType10: Arbitrary[ConsignmentItemType10] =
    Arbitrary {
      for {
        goodsItemNumber            <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveInts
        commodity                  <- arbitrary[CommodityType10]
      } yield ConsignmentItemType10(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDispatch = None,
        countryOfDestination = None,
        referenceNumberUCR = None,
        AdditionalSupplyChainActor = Nil,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil
      )
    }

  implicit lazy val arbitraryDepartureTransportMeansType01: Arbitrary[DepartureTransportMeansType01] =
    Arbitrary {
      for {
        sequenceNumber       <- arbitrary[BigInt]
        typeOfIdentification <- nonEmptyString
        identificationNumber <- nonEmptyString
        nationality          <- nonEmptyString
      } yield DepartureTransportMeansType01(
        sequenceNumber = sequenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality
      )
    }

  implicit lazy val arbitraryCommodityType10: Arbitrary[CommodityType10] =
    Arbitrary {
      for {
        descriptionOfGoods <- nonEmptyString
        goodsMeasure       <- arbitrary[GoodsMeasureType04]
      } yield CommodityType10(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = None,
        CommodityCode = None,
        DangerousGoods = Nil,
        GoodsMeasure = goodsMeasure
      )
    }

  implicit lazy val arbitraryGoodsMeasureType04: Arbitrary[GoodsMeasureType04] =
    Arbitrary {
      for {
        grossMass          <- arbitrary[BigDecimal]
        netMass            <- Gen.option(arbitrary[BigDecimal])
        supplementaryUnits <- Gen.option(arbitrary[BigDecimal])
      } yield GoodsMeasureType04(
        grossMass = grossMass,
        netMass = netMass,
        supplementaryUnits = supplementaryUnits
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

  implicit lazy val arbitraryCustomsOfficeOfDepartureType05: Arbitrary[CustomsOfficeOfDepartureType05] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDepartureType05(
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

  implicit lazy val arbitraryCustomsOfficeOfTransitDeclaredType06: Arbitrary[CustomsOfficeOfTransitDeclaredType06] =
    Arbitrary {
      for {
        sequenceNumber  <- arbitrary[BigInt]
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfTransitDeclaredType06(
        sequenceNumber = sequenceNumber,
        referenceNumber = referenceNumber,
        arrivalDateAndTimeEstimated = None
      )
    }

  implicit lazy val arbitraryLocationOfGoodsType02: Arbitrary[LocationOfGoodsType02] =
    Arbitrary {
      for {
        typeOfLocation            <- nonEmptyString
        qualifierOfIdentification <- nonEmptyString
      } yield LocationOfGoodsType02(
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

  implicit lazy val arbitraryLocationOfGoodsType04: Arbitrary[LocationOfGoodsType04] =
    Arbitrary {
      for {
        typeOfLocation            <- nonEmptyString
        qualifierOfIdentification <- nonEmptyString
      } yield LocationOfGoodsType04(
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

  implicit lazy val arbitraryPlaceOfLoadingType: Arbitrary[PlaceOfLoadingType] =
    Arbitrary {
      for {
        unLocode <- Gen.option(nonEmptyString)
        country  <- Gen.option(nonEmptyString)
        location <- Gen.option(nonEmptyString)
      } yield PlaceOfLoadingType(
        UNLocode = unLocode,
        country = country,
        location = location
      )
    }

  implicit lazy val arbitraryTransitOperationType02: Arbitrary[TransitOperationType02] =
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
      } yield TransitOperationType02(
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

  implicit lazy val arbitraryTransitOperationType03: Arbitrary[TransitOperationType03] =
    Arbitrary {
      for {
        lrn                       <- nonEmptyString
        declarationType           <- nonEmptyString
        additionalDeclarationType <- nonEmptyString
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
        bindingItinerary          <- arbitrary[Flag]
      } yield TransitOperationType03(
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

  implicit lazy val arbitraryTransitOperationType23: Arbitrary[TransitOperationType23] =
    Arbitrary {
      for {
        lrn       <- nonEmptyString
        limitDate <- Gen.option(arbitrary[XMLGregorianCalendar])
      } yield TransitOperationType23(
        LRN = lrn,
        limitDate = limitDate
      )
    }

  implicit lazy val arbitraryRepresentativeType06: Arbitrary[RepresentativeType06] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        status               <- nonEmptyString
        contactPerson        <- Gen.option(arbitrary[ContactPersonType03])
      } yield RepresentativeType06(
        identificationNumber = identificationNumber,
        status = status,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryTransportEquipmentType03: Arbitrary[TransportEquipmentType03] =
    Arbitrary {
      for {
        sequenceNumber                <- arbitrary[BigInt]
        containerIdentificationNumber <- Gen.option(nonEmptyString)
        numberOfSeals                 <- positiveInts
        seals                         <- arbitrary[Seq[SealType01]]
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType01]]
      } yield TransportEquipmentType03(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitrarySealType01: Arbitrary[SealType01] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
        identifier     <- nonEmptyString
      } yield SealType01(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryGoodsReferenceType01: Arbitrary[GoodsReferenceType01] =
    Arbitrary {
      for {
        sequenceNumber             <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveInts
      } yield GoodsReferenceType01(
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

  implicit lazy val arbitraryAddressType06: Arbitrary[AddressType06] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType06(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
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

  implicit lazy val arbitraryPostcodeAddressType: Arbitrary[PostcodeAddressType] =
    Arbitrary {
      for {
        houseNumber <- Gen.option(nonEmptyString)
        postcode    <- nonEmptyString
        country     <- nonEmptyString
      } yield PostcodeAddressType(
        houseNumber = houseNumber,
        postcode = postcode,
        country = country
      )
    }

  implicit lazy val arbitraryContactPersonType03: Arbitrary[ContactPersonType03] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType03(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryContactPersonType01: Arbitrary[ContactPersonType01] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType01(
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
