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
import itbase.ItSpecBase
import models.messages.{Consignment, HolderOfTheTransitProcedure, MessageData, TransitOperation}
import models.{EoriNumber, SensitiveFormats, UserAnswers}
import play.api.libs.json.Json
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends ItSpecBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  private val config: FrontendAppConfig        = app.injector.instanceOf[FrontendAppConfig]
  private val dateTimeService: DateTimeService = app.injector.instanceOf[DateTimeService]

  implicit private val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

  override protected val repository = new SessionRepository(mongoComponent, config, dateTimeService)

  private val ie015: MessageData = MessageData(
    CustomsOfficeOfDeparture = "c of dep",
    CustomsOfficeOfDestination = "c of des",
    TransitOperation = TransitOperation(
      LRN = None,
      limitDate = None,
      security = "sec",
      reducedDatasetIndicator = "0"
    ),
    Authorisation = None,
    HolderOfTheTransitProcedure = HolderOfTheTransitProcedure(
      identificationNumber = None,
      name = None,
      TIRHolderIdentificationNumber = None,
      ContactPerson = None,
      Address = None
    ),
    Representative = None,
    CustomsOfficeOfTransitDeclared = None,
    CustomsOfficeOfExitForTransitDeclared = None,
    Consignment = Consignment(
      containerIndicator = None,
      inlandModeOfTransport = None,
      modeOfTransportAtTheBorder = None,
      TransportEquipment = None,
      LocationOfGoods = None,
      DepartureTransportMeans = None,
      ActiveBorderTransportMeans = None,
      PlaceOfLoading = None,
      HouseConsignment = Nil
    )
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
  }
}
