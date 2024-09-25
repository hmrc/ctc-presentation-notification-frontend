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

package base

import generated._
import scalaxb.XMLCalendar

trait TestMessageData {

  val basicIe015: CC015CType = CC015CType(
    messageSequence1 = MESSAGESequence(
      messageSender = "",
      messageRecipient = "",
      preparationDateAndTime = XMLCalendar("2022-02-03T08:45:00.000000"),
      messageIdentification = "",
      messageType = CC015C,
      correlationIdentifier = None
    ),
    TransitOperation = TransitOperationType06(
      LRN = "",
      declarationType = "",
      additionalDeclarationType = "",
      TIRCarnetNumber = None,
      presentationOfTheGoodsDateAndTime = None,
      security = "",
      reducedDatasetIndicator = Number0,
      specificCircumstanceIndicator = None,
      communicationLanguageAtDeparture = None,
      bindingItinerary = Number0,
      limitDate = None
    ),
    Authorisation = Nil,
    CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
      referenceNumber = ""
    ),
    CustomsOfficeOfDestinationDeclared = CustomsOfficeOfDestinationDeclaredType01(
      referenceNumber = ""
    ),
    CustomsOfficeOfTransitDeclared = Nil,
    CustomsOfficeOfExitForTransitDeclared = Nil,
    HolderOfTheTransitProcedure = HolderOfTheTransitProcedureType14(
      identificationNumber = None,
      TIRHolderIdentificationNumber = None,
      name = None,
      Address = None,
      ContactPerson = None
    ),
    Representative = None,
    Guarantee = Nil,
    Consignment = ConsignmentType20(
      countryOfDispatch = None,
      countryOfDestination = None,
      containerIndicator = None,
      inlandModeOfTransport = None,
      modeOfTransportAtTheBorder = None,
      grossMass = BigDecimal(0),
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
    ),
    attributes = Map.empty
  )

  val completeIe015: CC015CType = CC015CType(
    messageSequence1 = MESSAGESequence(
      messageSender = "",
      messageRecipient = "",
      preparationDateAndTime = XMLCalendar("2022-02-03T08:45:00.000000"),
      messageIdentification = "",
      messageType = CC015C,
      correlationIdentifier = None
    ),
    TransitOperation = TransitOperationType06(
      LRN = "",
      declarationType = "",
      additionalDeclarationType = "",
      TIRCarnetNumber = None,
      presentationOfTheGoodsDateAndTime = None,
      security = "",
      reducedDatasetIndicator = Number0,
      specificCircumstanceIndicator = None,
      communicationLanguageAtDeparture = None,
      bindingItinerary = Number0,
      limitDate = Some(XMLCalendar("2020-01-01T09:30:00"))
    ),
    Authorisation = Nil,
    CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
      referenceNumber = ""
    ),
    CustomsOfficeOfDestinationDeclared = CustomsOfficeOfDestinationDeclaredType01(
      referenceNumber = ""
    ),
    CustomsOfficeOfTransitDeclared = Nil,
    CustomsOfficeOfExitForTransitDeclared = Nil,
    HolderOfTheTransitProcedure = HolderOfTheTransitProcedureType14(
      identificationNumber = None,
      TIRHolderIdentificationNumber = None,
      name = None,
      Address = None,
      ContactPerson = None
    ),
    Representative = None,
    Guarantee = Nil,
    Consignment = ConsignmentType20(
      countryOfDispatch = None,
      countryOfDestination = None,
      containerIndicator = Some(Number1),
      inlandModeOfTransport = None,
      modeOfTransportAtTheBorder = Some("1"),
      grossMass = BigDecimal(0),
      referenceNumberUCR = None,
      Carrier = None,
      Consignor = None,
      Consignee = None,
      AdditionalSupplyChainActor = Nil,
      TransportEquipment = Seq(
        TransportEquipmentType06(
          sequenceNumber = 1,
          containerIdentificationNumber = None,
          numberOfSeals = 0,
          Seal = Nil,
          GoodsReference = Nil
        )
      ),
      LocationOfGoods = Some(
        LocationOfGoodsType05(
          typeOfLocation = "A",
          qualifierOfIdentification = "T",
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
      ),
      DepartureTransportMeans = Nil,
      CountryOfRoutingOfConsignment = Nil,
      ActiveBorderTransportMeans = Seq(
        ActiveBorderTransportMeansType02(
          sequenceNumber = 1,
          customsOfficeAtBorderReferenceNumber = None,
          typeOfIdentification = None,
          identificationNumber = None,
          nationality = None,
          conveyanceReferenceNumber = None
        )
      ),
      PlaceOfLoading = Some(
        PlaceOfLoadingType03(
          UNLocode = None,
          country = None,
          location = None
        )
      ),
      PlaceOfUnloading = None,
      PreviousDocument = Nil,
      SupportingDocument = Nil,
      TransportDocument = Nil,
      AdditionalReference = Nil,
      AdditionalInformation = Nil,
      TransportCharges = None,
      HouseConsignment = Nil
    ),
    attributes = Map.empty
  )

  val encryptedIe015 =
    "Z+e9hWuo5kVyyUAoHrzIkPKprkwyASm34WcPW+HjVIrJD96PmY80FVPRTkKt9Y4yEwm6q4y2p3pyYWgpbKOBaeTH0GG4ASbE10YBaRCgAhkqyg4s29O328VYIiHA3xaEmrAHAPat7UoJ0mBxNNI+GozoCseNMCGyedu77FrRSl/X/pAs31wMUbLfZ4dvDp67ATTA4o+JY+Dk41kxwz/HR2xmMnlqrLYfv0wZ900bEYAhTI0OaSte5Q/SySm4E1dMxFj+SkO8wWUFca3heO4Twyk03Nn4vLL4TWqNSGJKNug9PIUjeQGpFDsBiwTDnG5R9rx1N6ZvLHAl/Zu9WAaHFbV1r4NdtRnuWjViKL2jqHBYEOQOYiFdD+KP255M/REq3Ju5rtWPUq1zYfZRfmzX+anagew3/UTkyHXlGDzN1m/1+lWHnvz9Jmd7WrajoI2N+03qp/ew0hEzZ1N6D0SP7aegSQ7/fZAhMMV5OHZOTxPtsEGTzRdSsk2Xzv6x78uGNgWOFlkItxRmskS57NKjgUW55fWdoISjM0VS2a3Zrxxj02Qez0MUcUZoukVeyhEUPG9I40QyvN7mC8KQ+CDMCDxTgzA+aTJJtWE9zUMIgjlQDp75w/9s9ETx3AwRGeR23xE45CEPG4DJwWNzFziYQMTBaA/76WoGugn1uewmYEbYd3Twn6rFctgS0QWz2zDo6zTpilTNQFvMnsMxOBhS7T/+VeKnvHzgsP3SZts77V96UHmPRGoYQ+rWNXZX+Qi/k2H523wUGp69K/BmrdSG8arMrZlpWJzjZwotfX3HiyTDRrNUAk4MF03yu2ghBEiFF49UTvGDJzxwh6Pte982F+yjbjLvU6/gmjV8QGxOhLwJEI9+cHk+S4bBUUjhxD6/TYDjiCpgIQsSJpcZ0rwIZCdD83NEaeROqzR4T16hLDpij4HJB6KUnS0UwVcvPrdIc1ttNDTIgC5m9WYWUJClutaBJQNU+p0AMXSnkdu4CspbDZHndKJBihF9QPAJmUp1uNWSTvbwQI8SYo3zIlZHZgpn0wlbBpxaWeNp4kuuxX0="

}
