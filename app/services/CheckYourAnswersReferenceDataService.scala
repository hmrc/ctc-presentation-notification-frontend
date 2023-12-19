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
import models.{LocationOfGoodsIdentification, LocationType}
import models.reference.{BorderMode, Country, CustomsOffice}
import services.CheckYourAnswersReferenceDataService.ReferenceDataNotFoundException
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersReferenceDataService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getLocationType(code: String)(implicit hc: HeaderCarrier): Future[LocationType] =
    for {
      locations <- referenceDataConnector.getTypesOfLocation()
      locationFound = locations
        .find(_.code == code)
        .getOrElse(referenceDataException("locationType", code, locations))
    } yield locationFound

  def getQualifierOfIdentification(code: String)(implicit hc: HeaderCarrier): Future[LocationOfGoodsIdentification] =
    for {
      qualifiers <- referenceDataConnector.getQualifierOfTheIdentifications()
      qualifierFound = qualifiers
        .find(_.code == code)
        .getOrElse(referenceDataException("qualifierOfIdentification", code, qualifiers))
    } yield qualifierFound

  def getCustomsOffice(countryCode: String)(id: String)(implicit hc: HeaderCarrier): Future[CustomsOffice] =
    for {
      customsOffices <- referenceDataConnector.getCustomsOfficesOfDepartureForCountry(countryCode)
      customsOffice = customsOffices
        .find(_.id == id)
        .getOrElse(referenceDataException("customsOffice", id, customsOffices))
    } yield customsOffice

  def getBorderMode(code: String)(implicit hc: HeaderCarrier): Future[BorderMode] =
    for {
      borderModes <- referenceDataConnector.getBorderModeCodes()
      modeFound = borderModes
        .find(_.code == code)
        .getOrElse(referenceDataException("borderMode", code, borderModes))

    } yield modeFound

  def getCountry(code: String)(implicit hc: HeaderCarrier): Future[Country] =
    for {
      countryCodes <- referenceDataConnector.getCountries("CountryCodesFullList")
      modeFound = countryCodes
        .find(_.code.code == code)
        .getOrElse(referenceDataException("countries", code, countryCodes))

    } yield modeFound

  private def referenceDataException[T](refName: String, refDataCode: String, listRefData: Seq[T]) =
    throw new ReferenceDataNotFoundException(refName, refDataCode, listRefData)

}

object CheckYourAnswersReferenceDataService {

  class ReferenceDataNotFoundException[T](refName: String, refDataCode: String, listRefData: Seq[T])
      extends Exception(s"Could not find the $refName code: '$refDataCode' from the available reference data: ${listRefData.mkString(", ")} ")
}
