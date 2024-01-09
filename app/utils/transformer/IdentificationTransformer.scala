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

package utils.transformer

import models.messages.Data
import models.reference.transport.border.active.Identification
import models.{Index, UserAnswers}
import pages.transport.border.active.IdentificationPage
import services.MeansOfTransportIdentificationTypesActiveService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class IdentificationTransformer @Inject() (identificationService: MeansOfTransportIdentificationTypesActiveService)(implicit
  ec: ExecutionContext
) {

  private def generatePageAndValues(departureDataIdentificationCodes: Seq[String],
                                    identificationList: Seq[Identification]
  ): Seq[(IdentificationPage, Identification)] =
    departureDataIdentificationCodes.zipWithIndex.flatMap {
      case (code, i) =>
        val index = Index(i)
        identificationList
          .find(_.code == code)
          .map(
            identification => (IdentificationPage(index), identification)
          )
    }

  def fromDepartureDataToUserAnswers(userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier
  ): Future[UserAnswers] =
    identificationService.getMeansOfTransportIdentificationTypesActive().flatMap {
      identifications =>
        val departureDataIdentificationCodes = userAnswers.departureData.Consignment.ActiveBorderTransportMeans.toList.flatten.flatMap(_.typeOfIdentification)
        val pageAndValueList                 = generatePageAndValues(departureDataIdentificationCodes, identifications)
        val updatedUserAnswersTry = pageAndValueList.foldLeft(Try(userAnswers)) {
          (accTry, pageAndValue) =>
            accTry.flatMap(_.set(pageAndValue._1, pageAndValue._2))
        }
        Future.fromTry(updatedUserAnswersTry)
    }

}
