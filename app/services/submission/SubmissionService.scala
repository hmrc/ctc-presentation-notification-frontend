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

import config.FrontendAppConfig
import connectors.DepartureMovementConnector
import generated.*
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.{Country, CustomsOffice, Item, LocationType, Nationality}
import models.{Coordinates, DynamicAddress, EoriNumber, Index, LocationOfGoodsIdentification, UserAnswers}
import pages.sections.houseConsignment.HouseConsignmentListSection
import pages.sections.transport.border.BorderActiveListSection
import pages.sections.transport.departureTransportMeans.TransportMeansListSection
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, InlandModePage, LimitDatePage}
import play.api.libs.functional.syntax.*
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
  messageIdentificationService: MessageIdentificationService,
  connector: DepartureMovementConnector,
  config: FrontendAppConfig
) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def submit(userAnswers: UserAnswers, departureId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    connector.submit(buildXml(userAnswers), departureId)

  def buildXml(userAnswers: UserAnswers): NodeSeq =
    toXML(transform(userAnswers), s"ncts:${CC170C.toString}", scope)

  private def transform(userAnswers: UserAnswers): CC170CType = {
    val officeOfDeparture = userAnswers.departureData.CustomsOfficeOfDeparture.referenceNumber
    implicit val reads: Reads[CC170CType] = for {
      transitOperation <- __.read[TransitOperationType23](transitOperationReads(userAnswers))
      representative   <- __.readNullableSafe[RepresentativeType06]
      consignment      <- __.read[ConsignmentType10]
    } yield CC170CType(
      messageSequence1 = messageSequence(userAnswers.eoriNumber, officeOfDeparture),
      TransitOperation = transitOperation,
      CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType05(
        referenceNumber = officeOfDeparture
      ),
      HolderOfTheTransitProcedure = holderOfTransit(userAnswers.departureData.HolderOfTheTransitProcedure),
      Representative = representative,
      Consignment = consignment,
      attributes = attributes
    )

    userAnswers.data.as[CC170CType]
  }

  def attributes: Map[String, DataRecord[?]] = {
    val phaseId = if (config.isPhase6Enabled) NCTS6 else NCTS5u461
    Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString(phaseId.toString, scope)))
  }

  def messageSequence(eoriNumber: EoriNumber, officeOfDeparture: String): MESSAGESequence =
    MESSAGESequence(
      messageSender = eoriNumber.value,
      messageRecipient = s"NTA.${officeOfDeparture.take(2)}",
      preparationDateAndTime = dateTimeService.currentDateTime,
      messageIdentification = messageIdentificationService.randomIdentifier,
      messageType = CC170C,
      correlationIdentifier = None
    )

  def transitOperationReads(userAnswers: UserAnswers): Reads[TransitOperationType23] =
    LimitDatePage.path.readNullable[LocalDate].map {
      limitDate => TransitOperationType23(userAnswers.lrn, limitDate)
    }

  def holderOfTransit(holderOfTransit: HolderOfTheTransitProcedureType23): HolderOfTheTransitProcedureType13 =
    HolderOfTheTransitProcedureType13(
      identificationNumber = holderOfTransit.identificationNumber,
      TIRHolderIdentificationNumber = holderOfTransit.TIRHolderIdentificationNumber,
      name = holderOfTransit.name,
      Address = holderOfTransit.Address.map {
        address =>
          AddressType14(
            streetAndNumber = address.streetAndNumber,
            postcode = address.postcode,
            city = address.city,
            country = address.country
          )
      }
    )

  implicit val representativeReads: Reads[RepresentativeType06] = {
    import pages.representative.*

    implicit val contactPersonReads: Reads[ContactPersonType03] = (
      NamePage.path.read[String] and
        RepresentativePhoneNumberPage.path.read[String] and
        None
    )(ContactPersonType03.apply)

    (
      EoriPage.path.read[String] and
        ("2": Reads[String]) and
        __.readNullableSafe[ContactPersonType03]
    )(RepresentativeType06.apply)
  }

  implicit val consignmentReads: Reads[ConsignmentType10] =
    for {
      containerIndicator         <- ContainerIndicatorPage.path.readNullable[Boolean].map(_.map(boolToFlag))
      inlandModeOfTransport      <- InlandModePage.path.readNullable[InlandMode].map(_.map(_.code))
      modeOfTransportAtTheBorder <- BorderModeOfTransportPage.path.readNullable[BorderMode].map(_.map(_.code))
      transportEquipment         <- EquipmentsSection.path.readArray[TransportEquipmentType03](transportEquipmentReads)
      locationOfGoods            <- __.read[LocationOfGoodsType02]
      departureTransportMeans    <- TransportMeansListSection.path.readArray[DepartureTransportMeansType01](departureTransportMeansReads)
      activeBorderTransportMeans <- BorderActiveListSection.path.readArray[ActiveBorderTransportMeansType03](activeBorderTransportMeansReads)
      placeOfLoading             <- __.readNullableSafe[PlaceOfLoadingType]
      houseConsignments          <- HouseConsignmentListSection.path.readArray[HouseConsignmentType06](houseConsignmentReads)
    } yield ConsignmentType10(
      containerIndicator = containerIndicator,
      inlandModeOfTransport = inlandModeOfTransport,
      modeOfTransportAtTheBorder = modeOfTransportAtTheBorder,
      TransportEquipment = transportEquipment,
      LocationOfGoods = locationOfGoods,
      DepartureTransportMeans = departureTransportMeans,
      ActiveBorderTransportMeans = activeBorderTransportMeans,
      PlaceOfLoading = placeOfLoading match {
        case Some(PlaceOfLoadingType(None, None, None)) => None
        case _                                          => placeOfLoading
      },
      HouseConsignment = houseConsignments match {
        case Nil => Seq(HouseConsignmentType06(1, Nil))
        case _   => houseConsignments
      }
    )

  implicit val locationOfGoodsReads: Reads[LocationOfGoodsType02] = {
    import pages.locationOfGoods.*

    implicit val customsOfficeReads: Reads[CustomsOfficeType02] =
      CustomsOfficeIdentifierPage.path.read[CustomsOffice].map(_.id).map(CustomsOfficeType02.apply)

    implicit val gnssReads: Reads[GNSSType] =
      CoordinatesPage.path.read[Coordinates].map {
        coordinates => GNSSType(coordinates.latitude, coordinates.longitude)
      }

    implicit val economicOperatorReads: Reads[EconomicOperatorType02] =
      EoriPage.path.read[String].map(EconomicOperatorType02.apply)

    implicit val addressReads: Reads[AddressType06] = (
      AddressPage.path.read[DynamicAddress] and
        CountryPage.path.read[Country]
    ).tupled.map {
      case (address, country) =>
        AddressType06(
          streetAndNumber = address.numberAndStreet,
          postcode = address.postalCode,
          city = address.city,
          country = country.code.code
        )
    }

    implicit val contactPersonReads: Reads[ContactPersonType01] = {
      import contact.*
      (
        NamePage.path.read[String] and
          PhoneNumberPage.path.read[String] and
          None
      )(ContactPersonType01.apply)
    }

    for {
      typeOfLocation            <- readInferred[LocationType](LocationTypePage, InferredLocationTypePage)
      qualifierOfIdentification <- readInferred[LocationOfGoodsIdentification](IdentificationPage, InferredIdentificationPage)
      authorisationNumber       <- AuthorisationNumberPage.path.readNullable[String]
      additionalIdentifier      <- AdditionalIdentifierPage.path.readNullable[String]
      unLocode                  <- UnLocodePage.path.readNullable[String]
      customsOffice             <- __.readNullableSafe[CustomsOfficeType02]
      gnss                      <- __.readNullableSafe[GNSSType]
      economicOperator          <- __.readNullableSafe[EconomicOperatorType02]
      address                   <- __.readNullableSafe[AddressType06]
      contactPerson             <- __.readNullableSafe[ContactPersonType01]
    } yield LocationOfGoodsType02(
      typeOfLocation = typeOfLocation.code,
      qualifierOfIdentification = qualifierOfIdentification.qualifier,
      authorisationNumber = authorisationNumber,
      additionalIdentifier = additionalIdentifier,
      UNLocode = unLocode,
      CustomsOffice = customsOffice,
      GNSS = gnss,
      EconomicOperator = economicOperator,
      Address = address,
      ContactPerson = contactPerson
    )
  }

  def departureTransportMeansReads(index: Index): Reads[DepartureTransportMeansType01] = {
    import models.reference.transport.transportMeans.TransportMeansIdentification
    import pages.transport.departureTransportMeans.*
    for {
      typeOfIdentification <- (__ \ TransportMeansIdentificationPage(index).toString).read[TransportMeansIdentification]
      identificationNumber <- (__ \ TransportMeansIdentificationNumberPage(index).toString).read[String]
      nationality          <- (__ \ TransportMeansNationalityPage(index).toString).read[Nationality]
    } yield DepartureTransportMeansType01(
      sequenceNumber = index.display,
      typeOfIdentification = typeOfIdentification.code,
      identificationNumber = identificationNumber,
      nationality = nationality.code
    )
  }

  def activeBorderTransportMeansReads(index: Index): Reads[ActiveBorderTransportMeansType03] = {
    import models.reference.transport.border.active.Identification
    import pages.transport.border.active.*
    for {
      customsOfficeAtBorderReferenceNumber <- (__ \ CustomsOfficeActiveBorderPage(index).toString).read[CustomsOffice]
      typeOfIdentification                 <- (__ \ IdentificationPage(index).toString).read[Identification]
      identificationNumber                 <- (__ \ IdentificationNumberPage(index).toString).read[String]
      nationality                          <- (__ \ NationalityPage(index).toString).read[Nationality]
      conveyanceReferenceNumber            <- (__ \ ConveyanceReferenceNumberPage(index).toString).readNullable[String]
    } yield ActiveBorderTransportMeansType03(
      sequenceNumber = index.display,
      customsOfficeAtBorderReferenceNumber = customsOfficeAtBorderReferenceNumber.id,
      typeOfIdentification = typeOfIdentification.code,
      identificationNumber = identificationNumber,
      nationality = nationality.code,
      conveyanceReferenceNumber = conveyanceReferenceNumber
    )
  }

  def transportEquipmentReads(equipmentIndex: Index): Reads[TransportEquipmentType03] = {
    import pages.sections.transport.equipment.*
    import pages.transport.equipment.index.*
    import pages.transport.equipment.index.seals.*

    def sealReads(sealIndex: Index): Reads[SealType01] =
      (__ \ SealIdentificationNumberPage(equipmentIndex, sealIndex).toString)
        .read[String]
        .map(SealType01(sealIndex.display, _))

    def goodsReferenceReads(itemIndex: Index): Reads[GoodsReferenceType01] =
      __.read[Item]
        .map(_.declarationGoodsItemNumber)
        .map(GoodsReferenceType01(itemIndex.display, _))

    for {
      containerIdNo   <- (__ \ ContainerIdentificationNumberPage(equipmentIndex).toString).readNullable[String]
      seals           <- (__ \ SealsSection(equipmentIndex).toString).readArray[SealType01](sealReads)
      goodsReferences <- (__ \ ItemsSection(equipmentIndex).toString).readArray[GoodsReferenceType01](goodsReferenceReads)
    } yield TransportEquipmentType03(
      sequenceNumber = equipmentIndex.display,
      containerIdentificationNumber = containerIdNo,
      numberOfSeals = seals.length,
      Seal = seals,
      GoodsReference = goodsReferences
    )
  }

  implicit val placeOfLoadingReads: Reads[PlaceOfLoadingType] = {
    import pages.loading.*
    (
      UnLocodePage.path.readNullable[String] and
        CountryPage.path.readNullable[Country].map(_.map(_.code.code)) and
        LocationPage.path.readNullable[String]
    )(PlaceOfLoadingType.apply)
  }

  def houseConsignmentReads(hcIndex: Index): Reads[HouseConsignmentType06] = {
    import pages.sections.houseConsignment.departureTransportMeans.*

    def departureTransportMeansReads(dtmIndex: Index): Reads[DepartureTransportMeansType01] = {
      import models.reference.transport.transportMeans.TransportMeansIdentification
      import pages.houseConsignment.index.departureTransportMeans.*

      for {
        typeOfIdentification <- (__ \ IdentificationPage(hcIndex, dtmIndex).toString).read[TransportMeansIdentification]
        identificationNumber <- (__ \ IdentificationNumberPage(hcIndex, dtmIndex).toString).read[String]
        nationality          <- (__ \ CountryPage(hcIndex, dtmIndex).toString).read[Nationality]
      } yield DepartureTransportMeansType01(
        sequenceNumber = dtmIndex.display,
        typeOfIdentification = typeOfIdentification.code,
        identificationNumber = identificationNumber,
        nationality = nationality.code
      )
    }

    (__ \ DepartureTransportMeansListSection(hcIndex).toString).readArray[DepartureTransportMeansType01](departureTransportMeansReads).map {
      departureTransportMeans =>
        HouseConsignmentType06(
          sequenceNumber = hcIndex.display,
          DepartureTransportMeans = departureTransportMeans
        )
    }
  }
}
