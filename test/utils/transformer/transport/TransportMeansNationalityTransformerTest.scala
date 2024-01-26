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
import base.TestMessageData.departureTransportMeansNationality
import models.reference.Nationality
import models.{Index, SelectableList}
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import pages.transport.departureTransportMeans.TransportMeansNationalityPage
import services.NationalitiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportMeansNationalityTransformerTest extends SpecBase {
  private val nationalitiesService                              = mock[NationalitiesService]
  private val transformer: TransportMeansNationalityTransformer = new TransportMeansNationalityTransformer(nationalitiesService)

  override def beforeEach() =
    reset(nationalitiesService)

  "TransportMeansNationalityTransformer" - {
    "fromDepartureDataToUserAnswers" - {
      "must return updated answers when the code from departure data can be found in service response" in {
        val transportMeansNationality = Nationality(departureTransportMeansNationality, "desc")
        when(nationalitiesService.getNationalities).thenReturn(Future.successful(SelectableList(List(transportMeansNationality))))

        val userAnswers = emptyUserAnswers
        val index       = Index(0)
        userAnswers.get(TransportMeansNationalityPage(index)) mustBe None

        whenReady(transformer.transform(hc)(userAnswers)) {
          updatedUserAnswers =>
            updatedUserAnswers.get(TransportMeansNationalityPage(index)) mustBe Some(transportMeansNationality)
        }
      }
    }

    "must return None when the code from departure data cannot be found in service response" in {
      val transportMeansNationality = Nationality("foo", "desc")
      when(nationalitiesService.getNationalities).thenReturn(Future.successful(SelectableList(List(transportMeansNationality))))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(TransportMeansNationalityPage(index)) mustBe None

      whenReady(transformer.transform(hc)(userAnswers)) {
        updatedUserAnswers =>
          updatedUserAnswers.get(TransportMeansNationalityPage(index)) mustBe None
      }
    }

    "must return failure if the service fails" in {
      when(nationalitiesService.getNationalities).thenReturn(Future.failed(new RuntimeException("")))

      val userAnswers = emptyUserAnswers
      val index       = Index(0)
      userAnswers.get(TransportMeansNationalityPage(index)) mustBe None

      whenReady[Throwable, Assertion](transformer.transform(hc)(userAnswers).failed) {
        _ mustBe an[Exception]
      }
    }
  }
}
