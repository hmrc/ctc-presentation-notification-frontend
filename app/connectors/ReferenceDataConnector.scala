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

package connectors

import cats.data.NonEmptyList
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference._
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{LocationOfGoodsIdentification, LocationType}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/$listName"
    http.GET[NonEmptyList[Country]](serviceUrl, headers = version2Header)
  }

  def getCountry(listName: String, code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Country]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val serviceUrl                         = s"${config.referenceDataUrl}/filtered-lists/$listName"
    http.GET[NonEmptyList[Country]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getCustomsOfficesOfTransitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "TRA")

  def getCustomsOfficesOfDestinationForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "DES")

  def getCustomsOfficesOfExitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "EXT")

  def getCustomsOfficesOfDepartureForCountry(
    countryCode: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode, "DEP")

  def getCustomsOffice(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CustomsOffice]] = {
    val queryParams = Seq("data.id" -> id)
    getCustomsOffices(queryParams)
  }

  def getCustomsSecurityAgreementAreaCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Country]] =
    getCountries("CountryCustomsSecurityAgreementArea")

  def getCountryCodesCTC()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Country]] =
    getCountries("CountryCodesCTC")

  def getAddressPostcodeBasedCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Country]] =
    getCountries("CountryAddressPostcodeBased")

  def getCountriesWithoutZip()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[CountryCode]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/CountryWithoutZip"
    http.GET[NonEmptyList[CountryCode]](serviceUrl, headers = version2Header)
  }

  def getUnLocodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[UnLocode]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/UnLocodeExtended"
    http.GET[NonEmptyList[UnLocode]](serviceUrl, headers = version2Header)
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[UnLocode]] = {
    val queryParams: Seq[(String, String)] = Seq("data.unLocodeExtendedCode" -> unLocode)
    val serviceUrl: String                 = s"${config.referenceDataUrl}/filtered-lists/UnLocodeExtended"
    http.GET[NonEmptyList[UnLocode]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Nationality]] = {
    val url = s"${config.referenceDataUrl}/lists/Nationality"
    http.GET[NonEmptyList[Nationality]](url, headers = version2Header)
  }

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Nationality]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val url                                = s"${config.referenceDataUrl}/filtered-lists/Nationality"
    http.GET[NonEmptyList[Nationality]](url, headers = version2Header, queryParams = queryParams)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[SpecificCircumstanceIndicator]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    http.GET[NonEmptyList[SpecificCircumstanceIndicator]](serviceUrl, headers = version2Header)
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[LocationType]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/TypeOfLocation"
    http.GET[NonEmptyList[LocationType]](serviceUrl, headers = version2Header)
  }

  def getTypeOfLocation(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[LocationType]] = {
    val queryParams: Seq[(String, String)] = Seq("data.type" -> code)
    val serviceUrl                         = s"${config.referenceDataUrl}/filtered-lists/TypeOfLocation"
    http.GET[NonEmptyList[LocationType]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[LocationOfGoodsIdentification]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    http.GET[NonEmptyList[LocationOfGoodsIdentification]](serviceUrl, headers = version2Header)
  }

  def getQualifierOfTheIdentification(
    qualifier: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[LocationOfGoodsIdentification]] = {
    val queryParams: Seq[(String, String)] = Seq("data.qualifier" -> qualifier)
    val serviceUrl                         = s"${config.referenceDataUrl}/filtered-lists/QualifierOfTheIdentification"
    http.GET[NonEmptyList[LocationOfGoodsIdentification]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  def getTransportModeCodes[T <: TransportMode[T]]()(implicit ec: ExecutionContext, hc: HeaderCarrier, rds: Reads[T]): Future[NonEmptyList[T]] = {
    val url = s"${config.referenceDataUrl}/lists/TransportModeCode"
    http.GET[NonEmptyList[T]](url, headers = version2Header)
  }

  def getTransportModeCode[T <: TransportMode[T]](code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier, rds: Reads[T]): Future[NonEmptyList[T]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val url                                = s"${config.referenceDataUrl}/filtered-lists/TransportModeCode"
    http.GET[NonEmptyList[T]](url, headers = version2Header, queryParams = queryParams)
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Identification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    http.GET[NonEmptyList[Identification]](url, headers = version2Header)
  }

  def getMeansOfTransportIdentificationTypeActive(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptyList[Identification]] = {
    val queryParams: Seq[(String, String)] = Seq("data.code" -> code)
    val url                                = s"${config.referenceDataUrl}/filtered-lists/TypeOfIdentificationofMeansOfTransportActive"
    http.GET[NonEmptyList[Identification]](url, headers = version2Header, queryParams = queryParams)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[TransportMeansIdentification]] = {
    val url = s"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http.GET[Seq[TransportMeansIdentification]](url, headers = version2Header)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  private def getCustomsOfficesForCountryAndRole(countryCode: String, role: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptyList[CustomsOffice]] = {

    val queryParams: Seq[(String, String)] = Seq(
      "data.countryId"  -> countryCode,
      "data.roles.role" -> role.toUpperCase.trim
    )

    getCustomsOffices(queryParams)
  }

  def getCustomsOfficeForId(id: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptyList[CustomsOffice]] = {

    val queryParams: Seq[(String, String)] = Seq("data.id" -> id)

    getCustomsOffices(queryParams)
  }

  private def getCustomsOffices(queryParams: Seq[(String, String)])(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptyList[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/filtered-lists/CustomsOffices"
    http.GET[NonEmptyList[CustomsOffice]](serviceUrl, headers = version2Header, queryParams = queryParams)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A]): HttpReads[NonEmptyList[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException
            case JsSuccess(head :: tail, _) =>
              NonEmptyList(head, tail)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
    }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException extends Exception("The reference data call was successful but the response body is empty.")
}
