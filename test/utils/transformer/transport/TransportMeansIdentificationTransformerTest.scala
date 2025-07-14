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
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import generated.DepartureTransportMeansType01
import generators.Generators
import models.Index
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import pages.transport.departureTransportMeans.TransportMeansIdentificationPage
import utils.transformer.departureTransportMeans.TransportMeansIdentificationTransformer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportMeansIdentificationTransformerTest extends SpecBase with Generators {
  private val referenceDataConnector                               = mock[ReferenceDataConnector]
  private val transformer: TransportMeansIdentificationTransformer = new TransportMeansIdentificationTransformer(referenceDataConnector)

  override def beforeEach(): Unit =
    reset(referenceDataConnector)

  "IdentificationTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        forAll(arbitrary[DepartureTransportMeansType01], arbitrary[TransportMeansIdentification]) {
          (departureTransportMeans, identification) =>
            when(referenceDataConnector.getMeansOfTransportIdentificationTypes())
              .thenReturn(Future.successful(Right(NonEmptySet.of(identification))))

            val userAnswers = setDepartureTransportMeansAnswersLens.replace(
              Seq(departureTransportMeans.copy(typeOfIdentification = identification.`type`))
            )(emptyUserAnswers)

            val result = transformer.transform.apply(userAnswers).futureValue
            result.get(TransportMeansIdentificationPage(Index(0))).value mustEqual identification
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      forAll(arbitrary[DepartureTransportMeansType01], nonEmptyString) {
        (departureTransportMeans, identificationCode) =>
          val identification = TransportMeansIdentification("foo", "desc")

          when(referenceDataConnector.getMeansOfTransportIdentificationTypes())
            .thenReturn(Future.successful(Right(NonEmptySet.of(identification))))

          val userAnswers = setDepartureTransportMeansAnswersLens.replace(
            Seq(departureTransportMeans.copy(typeOfIdentification = identificationCode))
          )(emptyUserAnswers)

          val result = transformer.transform.apply(userAnswers).futureValue
          result.get(TransportMeansIdentificationPage(Index(0))) must not be defined
      }
    }

    "must return failure if the service fails" in {
      when(referenceDataConnector.getMeansOfTransportIdentificationTypes())
        .thenReturn(Future.failed(new RuntimeException("")))

      whenReady[Throwable, Assertion](transformer.transform.apply(emptyUserAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
