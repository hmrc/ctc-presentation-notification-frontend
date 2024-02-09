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

import connectors.DepartureMovementConnector
import generated._
import models.messages.HolderOfTheTransitProcedure
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.{Country, CustomsOffice, Item, Nationality}
import models.{Coordinates, DynamicAddress, EoriNumber, Index, LocationOfGoodsIdentification, LocationType, PostalCodeAddress, UserAnswers}
import pages.sections.houseConsignment.HouseConsignmentListSection
import pages.sections.transport.border.BorderActiveListSection
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, InlandModePage, LimitDatePage}
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}
import scalaxb.DataRecord
import scalaxb.`package`.toXML
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future
import scala.xml.{NamespaceBinding, NodeSeq}

class SubmissionService @Inject() (
  dateTimeService: DateTimeService,
  connector: DepartureMovementConnector
) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def submit(userAnswers: UserAnswers, departureId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    connector.submit(buildXml(userAnswers), departureId)

  def buildXml(userAnswers: UserAnswers): NodeSeq =
    toXML(transform(userAnswers), s"ncts:${CC170C.toString}", scope)

  private def transform(userAnswers: UserAnswers): CC170CType = {
    val officeOfDeparture = userAnswers.departureData.CustomsOfficeOfDeparture
    implicit val reads: Reads[CC170CType] = for {
      transitOperation <- __.read[TransitOperationType24](transitOperationReads(userAnswers))
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
      transportEquipment         <- EquipmentsSection.path.readArray[TransportEquipmentType06](transportEquipmentReads)
      locationOfGoods            <- __.read[LocationOfGoodsType03]
      departureTransportMeans    <- TransportMeansSection.path.readObjectAsArray[DepartureTransportMeansType05](departureTransportMeansReads)
      activeBorderTransportMeans <- BorderActiveListSection.path.readArray[ActiveBorderTransportMeansType03](activeBorderTransportMeansReads)
      placeOfLoading             <- __.readNullableSafe[PlaceOfLoadingType03]
      houseConsignments          <- HouseConsignmentListSection.path.readArray[HouseConsignmentType06](houseConsignmentReads)
    } yield ConsignmentType08(
      containerIndicator = containerIndicator,
      inlandModeOfTransport = inlandModeOfTransport,
      modeOfTransportAtTheBorder = modeOfTransportAtTheBorder,
      TransportEquipment = transportEquipment,
      LocationOfGoods = locationOfGoods,
      DepartureTransportMeans = departureTransportMeans,
      ActiveBorderTransportMeans = activeBorderTransportMeans,
      PlaceOfLoading = placeOfLoading match {
        case Some(PlaceOfLoadingType03(None, None, None)) => None
        case _                                            => placeOfLoading
      },
      HouseConsignment = houseConsignments match {
        case Nil => Seq(HouseConsignmentType06("1", Nil))
        case _   => houseConsignments
      }
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
      typeOfLocation            <- readInferred[LocationType](LocationTypePage, InferredLocationTypePage)
      qualifierOfIdentification <- readInferred[LocationOfGoodsIdentification](IdentificationPage, InferredIdentificationPage)
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
      qualifierOfIdentification = qualifierOfIdentification.qualifier,
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

  def departureTransportMeansReads(index: Index): Reads[DepartureTransportMeansType05] = {
    import models.reference.transport.transportMeans.TransportMeansIdentification
    import pages.transport.departureTransportMeans._
    for {
      typeOfIdentification <- (__ \ TransportMeansIdentificationPage.toString).read[TransportMeansIdentification]
      identificationNumber <- (__ \ TransportMeansIdentificationNumberPage.toString).read[String]
      nationality          <- (__ \ TransportMeansNationalityPage.toString).read[Nationality]
    } yield DepartureTransportMeansType05(
      sequenceNumber = index.sequenceNumber,
      typeOfIdentification = typeOfIdentification.code,
      identificationNumber = identificationNumber,
      nationality = nationality.code
    )
  }

  def activeBorderTransportMeansReads(index: Index): Reads[ActiveBorderTransportMeansType03] = {
    import models.reference.transport.border.active.Identification
    import pages.transport.border.active._
    for {
      customsOfficeAtBorderReferenceNumber <- (__ \ CustomsOfficeActiveBorderPage(index).toString).read[CustomsOffice]
      typeOfIdentification                 <- (__ \ IdentificationPage(index).toString).read[Identification]
      identificationNumber                 <- (__ \ IdentificationNumberPage(index).toString).read[String]
      nationality                          <- (__ \ NationalityPage(index).toString).read[Nationality]
      conveyanceReferenceNumber            <- (__ \ ConveyanceReferenceNumberPage(index).toString).readNullable[String]
    } yield ActiveBorderTransportMeansType03(
      sequenceNumber = index.sequenceNumber,
      customsOfficeAtBorderReferenceNumber = customsOfficeAtBorderReferenceNumber.id,
      typeOfIdentification = typeOfIdentification.code,
      identificationNumber = identificationNumber,
      nationality = nationality.code,
      conveyanceReferenceNumber = conveyanceReferenceNumber
    )
  }

  def transportEquipmentReads(equipmentIndex: Index): Reads[TransportEquipmentType06] = {
    import pages.sections.transport.equipment._
    import pages.transport.equipment.index._
    import pages.transport.equipment.index.seals._

    def sealReads(sealIndex: Index): Reads[SealType05] =
      (__ \ SealIdentificationNumberPage(equipmentIndex, sealIndex).toString)
        .read[String]
        .map(SealType05(sealIndex.sequenceNumber, _))

    def goodsReferenceReads(itemIndex: Index): Reads[GoodsReferenceType02] =
      __.read[Item]
        .map(_.goodsItemNumber)
        .map(GoodsReferenceType02(itemIndex.sequenceNumber, _))

    for {
      containerIdNo   <- (__ \ ContainerIdentificationNumberPage(equipmentIndex).toString).readNullable[String]
      seals           <- (__ \ SealsSection(equipmentIndex).toString).readArray[SealType05](sealReads)
      goodsReferences <- (__ \ ItemsSection(equipmentIndex).toString).readArray[GoodsReferenceType02](goodsReferenceReads)
    } yield TransportEquipmentType06(equipmentIndex.sequenceNumber, containerIdNo, seals.length, seals, goodsReferences)
  }

  implicit val placeOfLoadingReads: Reads[PlaceOfLoadingType03] = {
    import pages.loading._
    (
      UnLocodePage.path.readNullable[String] and
        CountryPage.path.readNullable[Country].map(_.map(_.code.code)) and
        LocationPage.path.readNullable[String]
    )(PlaceOfLoadingType03.apply _)
  }

  def houseConsignmentReads(hcIndex: Index): Reads[HouseConsignmentType06] = {
    import pages.sections.houseConsignment.departureTransportMeans._

    def departureTransportMeansReads(dtmIndex: Index): Reads[DepartureTransportMeansType05] = {
      import models.reference.transport.transportMeans.TransportMeansIdentification
      import pages.houseConsignment.index.departureTransportMeans._

      for {
        typeOfIdentification <- (__ \ IdentificationPage(hcIndex, dtmIndex).toString).read[TransportMeansIdentification]
        identificationNumber <- (__ \ IdentificationNumberPage(hcIndex, dtmIndex).toString).read[String]
        nationality          <- (__ \ CountryPage(hcIndex, dtmIndex).toString).read[Nationality]
      } yield DepartureTransportMeansType05(
        sequenceNumber = dtmIndex.sequenceNumber,
        typeOfIdentification = typeOfIdentification.code,
        identificationNumber = identificationNumber,
        nationality = nationality.code
      )
    }

    (__ \ DepartureTransportMeansListSection(hcIndex).toString).readArray[DepartureTransportMeansType05](departureTransportMeansReads).map {
      departureTransportMeans =>
        HouseConsignmentType06(
          sequenceNumber = hcIndex.sequenceNumber,
          DepartureTransportMeans = departureTransportMeans
        )
    }
  }
}
