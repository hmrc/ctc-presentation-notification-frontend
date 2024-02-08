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

package services.submission

import generated._
import models.{EoriNumber, UserAnswers}
import pages.transport.LimitDatePage
import play.api.libs.json.Reads
import scalaxb.DataRecord
import services.DateTimeService

import java.time.LocalDate
import javax.inject.Inject
import scala.xml.NamespaceBinding

class SubmissionService @Inject() (dateTimeService: DateTimeService) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def transform(userAnswers: UserAnswers): CC170CType = {
    implicit val reads: Reads[CC170CType] = for {
      transitOperation <- transitOperation(userAnswers)
    } yield CC170CType(
      messageSequence1 = messageSequence(userAnswers.eoriNumber, userAnswers.departureData.CustomsOfficeOfDeparture),
      TransitOperation = transitOperation,
      CustomsOfficeOfDeparture = ???,
      HolderOfTheTransitProcedure = ???,
      Representative = ???,
      Consignment = ???,
      attributes = Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString("NCTS5.0", scope)))
    )

    userAnswers.data.as[CC170CType]
  }

  def messageSequence(eoriNumber: EoriNumber, officeOfDeparture: String): MESSAGESequence = {
    val messageType = CC170C
    MESSAGESequence(
      messageSender = eoriNumber.value,
      messagE_1Sequence2 = MESSAGE_1Sequence(
        messageRecipient = s"NTA.${officeOfDeparture.take(2)}",
        preparationDateAndTime = dateTimeService.now,
        messageIdentification = messageType.toString
      ),
      messagE_TYPESequence3 = MESSAGE_TYPESequence(
        messageType = messageType
      ),
      correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = None
      )
    )
  }

  def transitOperation(userAnswers: UserAnswers): Reads[TransitOperationType24] =
    LimitDatePage.path.readNullable[LocalDate].map {
      limitData => TransitOperationType24(userAnswers.lrn, limitData)
    }

}
