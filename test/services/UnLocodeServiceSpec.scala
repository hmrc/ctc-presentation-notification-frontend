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
import config.FrontendAppConfig
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.UnLocode
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnLocodeServiceSpec extends SpecBase {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val mockFrontendAppConfig: FrontendAppConfig     = mock[FrontendAppConfig]
  private val service                                      = new UnLocodeService(mockFrontendAppConfig, mockRefDataConnector)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "UnLocodeService" - {
    "getUnLocodeList" - {
      "when disableUnLocodeExtendedLookup is 'true' and the unLoCode is the correct format returns true" in {
        when(mockFrontendAppConfig.disableUnLocodeExtendedLookup).thenReturn(true)
        service.doesUnLocodeExist("DEAAL").futureValue mustEqual true
        verifyNoInteractions(mockRefDataConnector)
      }

      "when disableUnLocodeExtendedLookup is 'true' and the unLoCode is the correct format returns false" in {
        when(mockFrontendAppConfig.disableUnLocodeExtendedLookup).thenReturn(true)
        service.doesUnLocodeExist("InvalidValue").futureValue mustEqual false
        verifyNoInteractions(mockRefDataConnector)
      }

      "must return true when unLocode exists" in {

        val unLocode     = "DEAAL"
        val unLocodeItem = UnLocode(unLocode, "Place D")

        when(mockFrontendAppConfig.disableUnLocodeExtendedLookup).thenReturn(false)

        when(mockRefDataConnector.getUnLocode(anyString())(any(), any()))
          .thenReturn(Future.successful(Right(unLocodeItem)))

        service.doesUnLocodeExist(unLocode).futureValue mustEqual true
        verify(mockRefDataConnector).getUnLocode(ArgumentMatchers.eq(unLocode))(any(), any())
      }
    }

    "must return false when unLocode does not exist in reference data" in {

      val unLocode = "ABCDE"

      when(mockFrontendAppConfig.disableUnLocodeExtendedLookup).thenReturn(false)

      when(mockRefDataConnector.getUnLocode(anyString())(any(), any()))
        .thenReturn(Future.successful(Left(new NoReferenceDataFoundException(""))))

      service.doesUnLocodeExist(unLocode).futureValue mustEqual false
      verify(mockRefDataConnector).getUnLocode(ArgumentMatchers.eq(unLocode))(any(), any())
    }
  }
}
