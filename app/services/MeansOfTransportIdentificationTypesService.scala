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

import config.Constants.MeansOfTransportIdentification._
import config.Constants.TransportModeCode._
import connectors.ReferenceDataConnector
import models.reference.TransportMode.InlandMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.requests.DataRequest
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MeansOfTransportIdentificationTypesService @Inject() (
  referenceDataConnector: ReferenceDataConnector,
  transportModeCodesService: TransportModeCodesService
)(implicit ec: ExecutionContext) {

  def getMeansOfTransportIdentificationTypes(inlandMode: Option[InlandMode])(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Seq[TransportMeansIdentification]] =
    inlandMode match {
      case Some(InlandMode(_, _)) =>
        referenceDataConnector
          .getMeansOfTransportIdentificationTypes()
          .map(_.resolve())
          .map(_.toSeq)
          .flatMap(filter(_, Future.successful(inlandMode)))
      case None =>
        referenceDataConnector
          .getMeansOfTransportIdentificationTypes()
          .map(_.resolve())
          .map(_.toSeq)
          .flatMap(
            filter(
              _,
              transportModeCodesService.getInlandModes().map {
                _.find(_.code == request.userAnswers.departureData.Consignment.inlandModeOfTransport.getOrElse(Unknown))
              }
            )
          )
    }

  def getBorderMeansIdentification(code: String)(implicit hc: HeaderCarrier): Future[TransportMeansIdentification] =
    referenceDataConnector.getMeansOfTransportIdentificationType(code).map(_.resolve())

  private def filter(
    identificationTypes: Seq[TransportMeansIdentification],
    inlandMode: Future[Option[InlandMode]]
  ): Future[Seq[TransportMeansIdentification]] =
    inlandMode.map {
      case Some(InlandMode(code, _)) if code != Fixed && code != Unknown => identificationTypes.filter(_.code.startsWith(code))
      case _                                                             => identificationTypes.filterNot(_.code == UnknownIdentification)
    }
}
