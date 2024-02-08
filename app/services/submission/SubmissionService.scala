/*
 * Copyright 2024 HM Revenue & Customs
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

package services.submission

import generated._
import models.messages.HolderOfTheTransitProcedure
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.{Country, CustomsOffice}
import models.{Coordinates, DynamicAddress, EoriNumber, LocationOfGoodsIdentification, LocationType, PostalCodeAddress, UserAnswers}
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, InlandModePage, LimitDatePage}
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}
import scalaxb.DataRecord
import services.DateTimeService

import java.time.LocalDate
import javax.inject.Inject
import scala.xml.NamespaceBinding

class SubmissionService @Inject() (dateTimeService: DateTimeService) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def transform(userAnswers: UserAnswers): CC170CType = {
    val officeOfDeparture = userAnswers.departureData.CustomsOfficeOfDeparture
    implicit val reads: Reads[CC170CType] = for {
      transitOperation <- transitOperationReads(userAnswers)
      representative   <- __.readNullableSafe[RepresentativeType05]
      consignment      <- __.read[ConsignmentType08]
    } yield CC170CType(
      messageSequence1 = messageSequence(userAnswers.eoriNumber, officeOfDeparture),
      TransitOperation = transitOperation,
      CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
        referenceNumber = officeOfDeparture
      ),
      HolderOfTheTransitProcedure = holderOfTransit(userAnswers.departureData.HolderOfTheTransitProcedure),
      Representative = representative,
      Consignment = consignment,
      attributes = Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString("NCTS5.0", scope)))
    )

    userAnswers.data.as[CC170CType]
  }

  def messageSequence(eoriNumber: EoriNumber, officeOfDeparture: String): MESSAGESequence = {
    val messageType = CC170C
    MESSAGESequence(
      messageSender = eoriNumber.value,
      messagE_1Sequence2 = MESSAGE_1Sequence(
        messageRecipient = s"NTA.${officeOfDeparture.take(2)}",
        preparationDateAndTime = dateTimeService.now,
        messageIdentification = messageType.toString
      ),
      messagE_TYPESequence3 = MESSAGE_TYPESequence(
        messageType = messageType
      ),
      correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = None
      )
    )
  }

  def transitOperationReads(userAnswers: UserAnswers): Reads[TransitOperationType24] =
    LimitDatePage.path.readNullable[LocalDate].map {
      limitData => TransitOperationType24(userAnswers.lrn, limitData)
    }

  def holderOfTransit(holderOfTransit: HolderOfTheTransitProcedure): HolderOfTheTransitProcedureType19 =
    HolderOfTheTransitProcedureType19(
      identificationNumber = holderOfTransit.identificationNumber,
      TIRHolderIdentificationNumber = holderOfTransit.TIRHolderIdentificationNumber,
      name = holderOfTransit.name,
      Address = holderOfTransit.Address.map {
        address =>
          AddressType17(
            streetAndNumber = address.streetAndNumber,
            postcode = address.postcode,
            city = address.city,
            country = address.country
          )
      }
    )

  implicit val representativeReads: Reads[RepresentativeType05] = {
    import pages.representative._

    implicit val contactPersonReads: Reads[ContactPersonType05] = (
      NamePage.path.read[String] and
        RepresentativePhoneNumberPage.path.read[String] and
        None
    )(ContactPersonType05.apply _)

    (
      EoriPage.path.read[String] and
        ("2": Reads[String]) and
        __.readNullableSafe[ContactPersonType05]
    )(RepresentativeType05.apply _)
  }

  implicit val consignmentReads: Reads[ConsignmentType08] =
    for {
      containerIndicator         <- ContainerIndicatorPage.path.readNullable[Boolean].map(_.map(boolToFlag))
      inlandModeOfTransport      <- InlandModePage.path.readNullable[InlandMode].map(_.map(_.code))
      modeOfTransportAtTheBorder <- BorderModeOfTransportPage.path.readNullable[BorderMode].map(_.map(_.code))
      locationOfGoods            <- __.read[LocationOfGoodsType03]
    } yield ConsignmentType08(
      containerIndicator = containerIndicator,
      inlandModeOfTransport = inlandModeOfTransport,
      modeOfTransportAtTheBorder = modeOfTransportAtTheBorder,
      TransportEquipment = Nil, // TODO
      LocationOfGoods = locationOfGoods,
      DepartureTransportMeans = Nil, // TODO
      ActiveBorderTransportMeans = Nil, // TODO
      PlaceOfLoading = None, // TODO
      HouseConsignment = Nil // TODO
    )

  implicit val locationOfGoodsReads: Reads[LocationOfGoodsType03] = {
    import pages.locationOfGoods._

    implicit val customsOfficeReads: Reads[CustomsOfficeType02] =
      CustomsOfficeIdentifierPage.path.read[CustomsOffice].map(_.id).map(CustomsOfficeType02)

    implicit val gnssReads: Reads[GNSSType] =
      CoordinatesPage.path.read[Coordinates].map {
        coordinates => GNSSType(coordinates.latitude, coordinates.longitude)
      }

    implicit val economicOperatorReads: Reads[EconomicOperatorType03] =
      EoriPage.path.read[String].map(EconomicOperatorType03)

    implicit val addressReads: Reads[AddressType14] = (
      AddressPage.path.read[DynamicAddress] and
        CountryPage.path.read[Country]
    ).tupled.map {
      case (address, country) =>
        AddressType14(
          streetAndNumber = address.numberAndStreet,
          postcode = address.postalCode,
          city = address.city,
          country = country.code.code
        )
    }

    implicit val postcodeAddressReads: Reads[PostcodeAddressType02] =
      PostalCodePage.path.read[PostalCodeAddress].map {
        address =>
          PostcodeAddressType02(
            houseNumber = Some(address.streetNumber),
            postcode = address.postalCode,
            country = address.country.code.code
          )
      }

    implicit val contactPersonReads: Reads[ContactPersonType06] = {
      import contact._
      (
        NamePage.path.read[String] and
          PhoneNumberPage.path.read[String] and
          None
      )(ContactPersonType06.apply _)
    }

    for {
      typeOfLocation            <- LocationTypePage.path.read[LocationType] orElse InferredLocationTypePage.path.read[LocationType]
      qualifierOfIdentification <- LocationOfGoodsPage.path.read[LocationOfGoodsIdentification].map(_.qualifier)
      authorisationNumber       <- AuthorisationNumberPage.path.readNullable[String]
      additionalIdentifier      <- AdditionalIdentifierPage.path.readNullable[String]
      unLocode                  <- UnLocodePage.path.readNullable[String]
      customsOffice             <- __.readNullableSafe[CustomsOfficeType02]
      gnss                      <- __.readNullableSafe[GNSSType]
      economicOperator          <- __.readNullableSafe[EconomicOperatorType03]
      address                   <- __.readNullableSafe[AddressType14]
      postcodeAddress           <- __.readNullableSafe[PostcodeAddressType02]
      contactPerson             <- __.readNullableSafe[ContactPersonType06]
    } yield LocationOfGoodsType03(
      typeOfLocation = typeOfLocation.code,
      qualifierOfIdentification = qualifierOfIdentification,
      authorisationNumber = authorisationNumber,
      additionalIdentifier = additionalIdentifier,
      UNLocode = unLocode,
      CustomsOffice = customsOffice,
      GNSS = gnss,
      EconomicOperator = economicOperator,
      Address = address,
      PostcodeAddress = postcodeAddress,
      ContactPerson = contactPerson
    )
  }
}
