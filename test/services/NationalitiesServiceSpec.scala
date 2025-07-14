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

package services

import base.SpecBase
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.SelectableList
import models.reference.Nationality
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NationalitiesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new NationalitiesService(mockRefDataConnector)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "NationalitiesService" - {
    "getNationalities" - {
      "must return Nationality" in {

        val nationality = Nationality(code = "GB", description = "An archipelago in the North Atlantic Ocean")

        when(mockRefDataConnector.getNationalities()(any(), any()))
          .thenReturn(Future.successful(Right(NonEmptySet.of(nationality))))

        service.getNationalities().futureValue mustEqual SelectableList(Seq(nationality))
        verify(mockRefDataConnector).getNationalities()(any(), any())
      }
    }

  }
}
