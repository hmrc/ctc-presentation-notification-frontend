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

import config.Constants.QualifierOfTheIdentification._
import config.Constants.TypeOfLocation._
import connectors.ReferenceDataConnector
import models.LocationOfGoodsIdentification
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsIdentificationTypeService @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext) {

  def getLocationOfGoodsIdentificationTypes(locationType: String)(implicit hc: HeaderCarrier): Future[Seq[LocationOfGoodsIdentification]] = {
    def filter(locationOfGoods: Seq[LocationOfGoodsIdentification]): Seq[LocationOfGoodsIdentification] =
      locationType match {
        case DesignatedLocation =>
          locationOfGoods.filter(_.qualifierIsOneOf(CustomsOfficeIdentifier, UnlocodeIdentifier))
        case AuthorisedPlace =>
          locationOfGoods.filter(_.qualifierIsOneOf(AuthorisationNumberIdentifier))
        case ApprovedPlace =>
          locationOfGoods.filter(_.qualifierIsOneOf(EoriNumberIdentifier, CoordinatesIdentifier, UnlocodeIdentifier, AddressIdentifier))
        case Other =>
          locationOfGoods.filter(_.qualifierIsOneOf(CoordinatesIdentifier, UnlocodeIdentifier, AddressIdentifier))
        case _ =>
          locationOfGoods
      }

    referenceDataConnector
      .getQualifierOfTheIdentifications()
      .map(_.resolve())
      .map(_.toSeq)
      .map(filter)
  }

}
