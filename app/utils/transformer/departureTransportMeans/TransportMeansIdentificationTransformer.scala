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

package utils.transformer.departureTransportMeans

import cats.implicits.toFoldableOps
import connectors.ReferenceDataConnector
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Index, UserAnswers}
import pages.transport.departureTransportMeans.TransportMeansIdentificationPage
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer
import services.RichResponses

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = TransportMeansIdentification
  override type ExtractedTypeInDepartureData = String

  def transform(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      fetchReferenceData = () => referenceDataConnector.getMeansOfTransportIdentificationTypes().map(_.resolve()).map(_.toList),
      extractDataFromDepartureData = _.departureData.Consignment.DepartureTransportMeans.flatMap(_.typeOfIdentification),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(
    departureDataIdentificationCodes: Seq[String],
    identificationList: Seq[TransportMeansIdentification]
  ): Seq[(TransportMeansIdentificationPage, TransportMeansIdentification)] =
    departureDataIdentificationCodes.zipWithIndex.collect {
      case (code, i) =>
        val index = Index(i)
        identificationList
          .find(_.code == code)
          .map(
            identification => (TransportMeansIdentificationPage(index), identification)
          )
    }.flatten

}
