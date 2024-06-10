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

package repositories

import config.FrontendAppConfig
import generated.{
  CC015C,
  CC015CType,
  CORRELATION_IDENTIFIERSequence,
  ConsignmentType20,
  CustomsOfficeOfDepartureType03,
  CustomsOfficeOfDestinationDeclaredType01,
  HolderOfTheTransitProcedureType14,
  MESSAGESequence,
  MESSAGE_1Sequence,
  MESSAGE_TYPESequence,
  Number0,
  TransitOperationType06
}
import itbase.ItSpecBase
import models.{EoriNumber, SensitiveFormats, UserAnswers}
import play.api.libs.json.Json
import scalaxb.XMLCalendar
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends ItSpecBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  private val config: FrontendAppConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val dateTimeService: DateTimeService = app.injector.instanceOf[DateTimeService]

  implicit private val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

  override protected val repository = new SessionRepository(mongoComponent, config, dateTimeService)

  private val ie015: CC015CType = CC015CType(
    messageSequence1 = MESSAGESequence(
      messageSender = "",
      messagE_1Sequence2 = MESSAGE_1Sequence(
        messageRecipient = "",
        preparationDateAndTime = XMLCalendar("2022-02-03T08:45:00.000000"),
        messageIdentification = ""
      ),
      messagE_TYPESequence3 = MESSAGE_TYPESequence(
        messageType = CC015C
      ),
      correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = None
      )
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

  private val userAnswers1 = UserAnswers(
    id = "id1",
    eoriNumber = EoriNumber("eori1"),
    lrn = "lrn1",
    data = Json.obj("bar" -> "foo"),
    lastUpdated = dateTimeService.currentInstant,
    departureData = ie015
  )

  private val userAnswers2 = UserAnswers(
    id = "id2",
    eoriNumber = EoriNumber("eori2"),
    lrn = "lrn2",
    data = Json.obj("bar" -> "foo"),
    lastUpdated = dateTimeService.currentInstant,
    departureData = ie015
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    insert(userAnswers1).futureValue
    insert(userAnswers2).futureValue
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given a departure ID" in {

        val result = repository.get("id1").futureValue

        result.value.id mustBe userAnswers1.id
        result.value.eoriNumber mustBe userAnswers1.eoriNumber
        result.value.lrn mustBe userAnswers1.lrn
        result.value.data mustBe userAnswers1.data
        result.value.departureData mustBe userAnswers1.departureData
      }

      "must return None when no UserAnswers match departure ID" in {

        val result = repository.get("id3").futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val userAnswers = UserAnswers(
          id = "id3",
          eoriNumber = EoriNumber("eori3"),
          lrn = "lrn3",
          data = Json.obj("bar" -> "foo"),
          lastUpdated = dateTimeService.currentInstant,
          departureData = ie015
        )

        val setResult = repository.set(userAnswers).futureValue

        val getResult = repository.get("id3").futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers.id
        getResult.eoriNumber mustBe userAnswers.eoriNumber
        getResult.lrn mustBe userAnswers.lrn
        getResult.data mustBe userAnswers.data
        getResult.departureData mustBe userAnswers.departureData
      }
    }

    "remove" - {

      "must remove document when given a valid departure ID" in {

        repository.get("id1").futureValue mustBe defined

        repository.remove("id1").futureValue

        repository.get("id1").futureValue must not be defined
      }
    }
  }
}
