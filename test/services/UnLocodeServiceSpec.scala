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
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.UnLocode
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnLocodeServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new UnLocodeService(mockRefDataConnector)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "UnLocodeService" - {
    "getUnLocodeList" - {
      "must return true when unLocode exists" in {

        val unLocode     = "DEAAL"
        val unLocodeItem = UnLocode(unLocode, "Place D")

        when(mockRefDataConnector.getUnLocode(anyString())(any(), any()))
          .thenReturn(Future.successful(Right(unLocodeItem)))

        service.doesUnLocodeExist(unLocode).futureValue mustEqual true
        verify(mockRefDataConnector).getUnLocode(ArgumentMatchers.eq(unLocode))(any(), any())
      }
    }

    "must return false when unLocode does not exist in reference data" in {

      val unLocode = "ABCDE"

      when(mockRefDataConnector.getUnLocode(anyString())(any(), any()))
        .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

      service.doesUnLocodeExist(unLocode).futureValue mustEqual false
      verify(mockRefDataConnector).getUnLocode(ArgumentMatchers.eq(unLocode))(any(), any())
    }
  }
}
