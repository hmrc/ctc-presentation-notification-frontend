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
import models.reference.UnLocode
import models.SelectableList
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnLocodeServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new UnLocodeService(mockRefDataConnector)

  private val unLocode1    = UnLocode("D", "Place D")
  private val unLocode2    = UnLocode("C", "Place C")
  private val unLocode3    = UnLocode("B", "Place B")
  private val unLocode4    = UnLocode("A", "Place A")
  private val unLocodeList = Seq(unLocode1, unLocode2, unLocode3, unLocode4)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "UnLocodeService" - {
    "getUnLocodeList" - {
      "must return a list of sorted location codes" in {
        when(mockRefDataConnector.getUnLocodes()(any(), any()))
          .thenReturn(Future.successful(unLocodeList))

        service.getUnLocodeList.futureValue mustBe
          SelectableList(Seq(unLocode4, unLocode3, unLocode2, unLocode1))

        verify(mockRefDataConnector).getUnLocodes()(any(), any())
      }
    }
  }
}
