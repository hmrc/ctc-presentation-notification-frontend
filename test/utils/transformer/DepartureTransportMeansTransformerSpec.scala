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
import generated.DepartureTransportMeansType01
import generators.Generators
import models.Index
import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.CheckYourAnswersReferenceDataService

import scala.concurrent.Future

class DepartureTransportMeansTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private lazy val mockReferenceDataService: CheckYourAnswersReferenceDataService = mock[CheckYourAnswersReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[CheckYourAnswersReferenceDataService].toInstance(mockReferenceDataService)
      )

  private val transformer = app.injector.instanceOf[DepartureTransportMeansTransformer]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" - {
    "when consignment level" - {
      import pages.transport.departureTransportMeans.*

      def departureTransportMeansGen(implicit a: Arbitrary[DepartureTransportMeansType01]): Gen[Seq[DepartureTransportMeansType01]] =
        listWithMaxLength[DepartureTransportMeansType01]()(a)
          .map {
            _.distinctBy(_.typeOfIdentification)
              .distinctBy(_.nationality)
          }

      "when values defined" in {
        forAll(departureTransportMeansGen(arbitraryDepartureTransportMeansType01)) {
          departureTransportMeans =>
            beforeEach()

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                when(mockReferenceDataService.getMeansOfTransportIdentificationType(eqTo(dtm.typeOfIdentification))(any()))
                  .thenReturn(Future.successful(TransportMeansIdentification(dtm.typeOfIdentification, i.toString)))

                when(mockReferenceDataService.getNationality(eqTo(dtm.nationality))(any()))
                  .thenReturn(Future.successful(Nationality(dtm.nationality, i.toString)))
            }

            val result = transformer.transform(departureTransportMeans).apply(emptyUserAnswers).futureValue

            departureTransportMeans.zipWithIndex.map {
              case (dtm, i) =>
                val dtmIndex = Index(i)

                result.getValue(TransportMeansIdentificationPage(dtmIndex)).code mustEqual dtm.typeOfIdentification
                result.getValue(TransportMeansIdentificationPage(dtmIndex)).description mustEqual i.toString
                result.getValue(TransportMeansIdentificationNumberPage(dtmIndex)) mustEqual dtm.identificationNumber
                result.getValue(TransportMeansNationalityPage(dtmIndex)).code mustEqual dtm.nationality
                result.getValue(TransportMeansNationalityPage(dtmIndex)).description mustEqual i.toString
            }
        }
      }
    }
  }
}
