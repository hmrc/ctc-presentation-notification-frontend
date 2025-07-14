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

package utils.transformer.transport

import base.SpecBase
import generated.DepartureTransportMeansType01
import generators.Generators
import models.reference.Nationality
import models.{Index, SelectableList}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.departureTransportMeans.TransportMeansNationalityPage
import services.NationalitiesService
import utils.transformer.departureTransportMeans.TransportMeansNationalityTransformer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportMeansNationalityTransformerTest extends SpecBase with Generators {
  private val nationalitiesService                              = mock[NationalitiesService]
  private val transformer: TransportMeansNationalityTransformer = new TransportMeansNationalityTransformer(nationalitiesService)

  override def beforeEach(): Unit =
    reset(nationalitiesService)

  "TransportMeansNationalityTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        forAll(arbitrary[DepartureTransportMeansType01], arbitrary[Nationality]) {
          (departureTransportMeans, nationality) =>
            when(nationalitiesService.getNationalities())
              .thenReturn(Future.successful(SelectableList(List(nationality))))

            val userAnswers = setDepartureTransportMeansAnswersLens.replace(
              Seq(departureTransportMeans.copy(nationality = nationality.code))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(TransportMeansNationalityPage(Index(0))).value mustEqual nationality
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      forAll(arbitrary[DepartureTransportMeansType01], arbitrary[Nationality]) {
        (departureTransportMeans, nationality) =>
          when(nationalitiesService.getNationalities())
            .thenReturn(Future.successful(SelectableList(Nil)))

          val userAnswers = setDepartureTransportMeansAnswersLens.replace(
            Seq(departureTransportMeans.copy(nationality = nationality.code))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(TransportMeansNationalityPage(Index(0))) must not be defined
      }
    }

    "must return failure if the service fails" in {
      when(nationalitiesService.getNationalities())
        .thenReturn(Future.failed(new RuntimeException("")))

      whenReady[Throwable, Assertion](transformer.transform.apply(emptyUserAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
