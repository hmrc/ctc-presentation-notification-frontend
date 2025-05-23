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

import config.Constants.MeansOfTransportIdentification.UnknownIdentification
import connectors.ReferenceDataConnector
import models.Index
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.DataRequest
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansIdentificationTypesService @Inject() (referenceDataConnector: ReferenceDataConnector, transportModeCodesService: TransportModeCodesService)(
  implicit ec: ExecutionContext
) {

  def getMeansOfTransportIdentificationTypes(index: Index, inlandModeOfTransport: Option[InlandMode])(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Seq[TransportMeansIdentification]] = inlandModeOfTransport match {
    case Some(InlandMode(_, _)) =>
      referenceDataConnector
        .getMeansOfTransportIdentificationTypes()
        .map(_.resolve())
        .map(_.toSeq)
        .flatMap(filter(_, index, Future.successful(inlandModeOfTransport)))
    case None =>
      referenceDataConnector
        .getMeansOfTransportIdentificationTypes()
        .map(_.resolve())
        .map(_.toSeq)
        .flatMap(
          filter(
            _,
            index,
            transportModeCodesService.getInlandModes().map {
              inlandModes =>
                request.userAnswers.departureData.Consignment.inlandModeOfTransport.flatMap(
                  inlandModeIE15 => inlandModes.find(_.code == inlandModeIE15)
                )
            }
          )
        )
  }

  private def filter(
    transportMeansIdentificationsTypes: Seq[TransportMeansIdentification],
    index: Index,
    inlandModeOfTransport: Future[Option[InlandMode]]
  ): Future[Seq[TransportMeansIdentification]] = {
    val identificationTypesExcludingUnknown = transportMeansIdentificationsTypes.filterNot(_.code == UnknownIdentification)

    inlandModeOfTransport map {
      case Some(InlandMode(code, _)) if index.isFirst =>
        identificationTypesExcludingUnknown.filter(_.code.startsWith(code))
      case _ => identificationTypesExcludingUnknown
    }
  }
}
