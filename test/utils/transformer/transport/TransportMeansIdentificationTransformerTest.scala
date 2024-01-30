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
import base.TestMessageData.departureTransportMeansIdentification
import cats.data.{NonEmptyList, NonEmptySet}
import connectors.ReferenceDataConnector
import models.Index
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.transport.departureTransportMeans.TransportMeansIdentificationPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportMeansIdentificationTransformerTest extends SpecBase {
  private val referenceDataConnector                               = mock[ReferenceDataConnector]
  private val transformer: TransportMeansIdentificationTransformer = new TransportMeansIdentificationTransformer(referenceDataConnector)

  override def beforeEach() =
    reset(referenceDataConnector)

  "IdentificationTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        val transportMeansIdentification = TransportMeansIdentification(departureTransportMeansIdentification, "desc")
        when(referenceDataConnector.getMeansOfTransportIdentificationTypes).thenReturn(Future.successful(NonEmptySet.of(transportMeansIdentification)))

        val userAnswers = emptyUserAnswers
        val index       = Index(0)
        userAnswers.get(TransportMeansIdentificationPage(index)) mustBe None

        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(TransportMeansIdentificationPage(index)) mustBe Some(transportMeansIdentification)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      val transportMeansIdentification = TransportMeansIdentification("foo", "desc")
      when(referenceDataConnector.getMeansOfTransportIdentificationTypes).thenReturn(Future.successful(NonEmptySet.of(transportMeansIdentification)))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(TransportMeansIdentificationPage(index)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(TransportMeansIdentificationPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(referenceDataConnector.getMeansOfTransportIdentificationTypes).thenReturn(Future.failed(new RuntimeException("")))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(TransportMeansIdentificationPage(index)) mustBe None

      whenReady[Throwable, Assertion](transformer.transform(hc)(userAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
