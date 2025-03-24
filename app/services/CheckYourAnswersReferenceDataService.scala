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

import connectors.ReferenceDataConnector
import models.LocationType
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.reference.{Country, Nationality}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersReferenceDataService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getLocationType(code: String)(implicit hc: HeaderCarrier): Future[LocationType] =
    referenceDataConnector.getTypeOfLocation(code).map(_.resolve())

  def getMeansOfTransportIdentificationType(code: String)(implicit hc: HeaderCarrier): Future[TransportMeansIdentification] =
    referenceDataConnector.getMeansOfTransportIdentificationType(code).map(_.resolve())

  def getBorderMeansIdentification(code: String)(implicit hc: HeaderCarrier): Future[Identification] =
    referenceDataConnector.getMeansOfTransportIdentificationTypeActive(code).map(_.resolve())

  def getNationality(code: String)(implicit hc: HeaderCarrier): Future[Nationality] =
    referenceDataConnector.getNationality(code).map(_.resolve())

  def getCountry(code: String)(implicit hc: HeaderCarrier): Future[Country] =
    referenceDataConnector.getCountry("CountryCodesFullList", code).map(_.resolve())

}
