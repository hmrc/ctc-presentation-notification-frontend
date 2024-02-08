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
import models.messages.HolderOfTheTransitProcedure
import models.{EoriNumber, UserAnswers}
import pages.transport.LimitDatePage
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads
import scalaxb.DataRecord
import services.DateTimeService

import java.time.LocalDate
import javax.inject.Inject
import scala.xml.NamespaceBinding

class SubmissionService @Inject() (dateTimeService: DateTimeService) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def transform(userAnswers: UserAnswers): CC170CType = {
    val officeOfDeparture = userAnswers.departureData.CustomsOfficeOfDeparture
    implicit val reads: Reads[CC170CType] = for {
      transitOperation <- transitOperation(userAnswers)
      representative   <- representative()
    } yield CC170CType(
      messageSequence1 = messageSequence(userAnswers.eoriNumber, officeOfDeparture),
      TransitOperation = transitOperation,
      CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(
        referenceNumber = officeOfDeparture
      ),
      HolderOfTheTransitProcedure = holderOfTransit(userAnswers.departureData.HolderOfTheTransitProcedure),
      Representative = representative,
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

  def holderOfTransit(holderOfTransit: HolderOfTheTransitProcedure): HolderOfTheTransitProcedureType19 =
    HolderOfTheTransitProcedureType19(
      identificationNumber = holderOfTransit.identificationNumber,
      TIRHolderIdentificationNumber = holderOfTransit.TIRHolderIdentificationNumber,
      name = holderOfTransit.name,
      Address = holderOfTransit.Address.map {
        address =>
          AddressType17(
            streetAndNumber = address.streetAndNumber,
            postcode = address.postcode,
            city = address.city,
            country = address.country
          )
      }
    )

  def representative(): Reads[Option[RepresentativeType05]] = {
    import pages.representative._
    EoriPage.path.readNullable[String].flatMap {
      case Some(identificationNumber) =>
        (
          NamePage.path.readNullable[String] and
            RepresentativePhoneNumberPage.path.readNullable[String]
        ).tupled
          .map {
            case (Some(name), Some(phoneNumber)) => Some(ContactPersonType05(name, phoneNumber, None))
            case _                               => None
          }
          .map {
            contactPerson =>
              Some(
                RepresentativeType05(
                  identificationNumber = identificationNumber,
                  status = "2",
                  ContactPerson = contactPerson
                )
              )
          }
      case _ => None
    }
  }
}
