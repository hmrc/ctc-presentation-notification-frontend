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

package utils.transformer

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.ActiveBorderTransportMeansType03
import generators.Generators
import models.Index
import models.reference.transport.border.active.Identification
import models.reference.{CustomsOffice, Nationality}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{CheckYourAnswersReferenceDataService, CustomsOfficesService, MeansOfTransportIdentificationTypesActiveService}

import scala.concurrent.Future

class ActiveBorderTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val mockCustomsReferenceDataService: CustomsOfficesService                      = mock[CustomsOfficesService]
  private lazy val mockIdentificationService: MeansOfTransportIdentificationTypesActiveService = mock[MeansOfTransportIdentificationTypesActiveService]
  private lazy val mockNationalityService: CheckYourAnswersReferenceDataService                = mock[CheckYourAnswersReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CustomsOfficesService].toInstance(mockCustomsReferenceDataService),
        bind[MeansOfTransportIdentificationTypesActiveService].toInstance(mockIdentificationService),
        bind[CheckYourAnswersReferenceDataService].toInstance(mockNationalityService)
      )

  private val transformer = app.injector.instanceOf[ActiveBorderTransportMeansTransformer]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCustomsReferenceDataService)
  }

  "must transform data" - {
    "when values defined" in {
      forAll(listWithMaxLength[ActiveBorderTransportMeansType03]().map(_.map(_.copy(conveyanceReferenceNumber = Some("value"))))) {
        activeBorderTransportMeans =>
          beforeEach()

          activeBorderTransportMeans.zipWithIndex.map {
            case (atm, i) =>
              when(mockCustomsReferenceDataService.getCustomsOfficeById(eqTo(atm.customsOfficeAtBorderReferenceNumber))(any()))
                .thenReturn(Future.successful(CustomsOffice(atm.customsOfficeAtBorderReferenceNumber, i.toString, Some("12345"))))

              when(mockIdentificationService.getBorderMeansIdentification(eqTo(atm.typeOfIdentification))(any()))
                .thenReturn(Future.successful(Identification(atm.typeOfIdentification, i.toString)))

              when(mockNationalityService.getNationality(eqTo(atm.nationality))(any()))
                .thenReturn(Future.successful(Nationality(atm.nationality, i.toString)))
          }

          val result = transformer.transform(activeBorderTransportMeans).apply(emptyUserAnswers).futureValue
          result.get(AddBorderMeansOfTransportYesNoPage).value mustEqual true

          activeBorderTransportMeans.zipWithIndex.map {
            case (atm, i) =>
              val atmIndex = Index(i)

              result.getValue(CustomsOfficeActiveBorderPage(atmIndex)).id mustEqual atm.customsOfficeAtBorderReferenceNumber
              result.getValue(IdentificationPage(atmIndex)).code mustEqual atm.typeOfIdentification
              result.getValue(IdentificationNumberPage(atmIndex)) mustEqual atm.identificationNumber
              result.getValue(NationalityPage(atmIndex)).code mustEqual atm.nationality
              result.get(AddConveyanceReferenceYesNoPage(atmIndex)).value mustEqual true
              result.get(ConveyanceReferenceNumberPage(atmIndex)) mustEqual atm.conveyanceReferenceNumber

          }
      }
    }
  }
}
