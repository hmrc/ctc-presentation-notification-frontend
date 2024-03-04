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

import cats.Order
import cats.data.NonEmptySet
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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/$listName"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
  }

  def getCountry(listName: String, code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Country] = {
    val url = url"${config.referenceDataUrl}/lists/$listName"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
      .map(_.head)
  }

  def getCustomsOfficesOfTransitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "TRA")

  def getCustomsOfficesOfDestinationForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "DES")

  def getCustomsOfficesOfExitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "EXT")

  def getCustomsOfficesOfDepartureForCountry(
    countryCode: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode, "DEP")

  def getCustomsSecurityAgreementAreaCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] =
    getCountries("CountryCustomsSecurityAgreementArea")

  def getCountryCodesCTC()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] =
    getCountries("CountryCodesCTC")

  def getAddressPostcodeBasedCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] =
    getCountries("CountryAddressPostcodeBased")

  def getCountriesWithoutZip()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CountryCode]] = {
    val url = url"${config.referenceDataUrl}/lists/CountryWithoutZip"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CountryCode]]
  }

  def getUnLocodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[UnLocode]] = {
    val url = url"${config.referenceDataUrl}/lists/UnLocodeExtended"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[UnLocode]]
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UnLocode] = {
    val url = url"${config.referenceDataUrl}/lists/UnLocodeExtended"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.unLocodeExtendedCode" -> unLocode))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[UnLocode]]
      .map(_.head)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Nationality]] = {
    val url = url"${config.referenceDataUrl}/lists/Nationality"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Nationality]]
  }

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Nationality] = {
    val url = url"${config.referenceDataUrl}/lists/Nationality"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Nationality]]
      .map(_.head)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[SpecificCircumstanceIndicator]] = {
    val url = url"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[SpecificCircumstanceIndicator]]
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[LocationType]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationType]]
  }

  def getTypeOfLocation(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[LocationType] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.type" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationType]]
      .map(_.head)
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[LocationOfGoodsIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationOfGoodsIdentification]]
  }

  def getQualifierOfTheIdentification(
    qualifier: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[LocationOfGoodsIdentification] = {
    val url = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.qualifier" -> qualifier))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationOfGoodsIdentification]]
      .map(_.head)
  }

  def getTransportModeCodes[T <: TransportMode[T]]()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[NonEmptySet[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[T]]
  }

  def getTransportModeCode[T <: TransportMode[T]](
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, rds: Reads[T], order: Order[T]): Future[T] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[T]]
      .map(_.head)
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Identification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Identification]]
  }

  def getMeansOfTransportIdentificationTypeActive(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Identification] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.code" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Identification]]
      .map(_.head)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[TransportMeansIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[TransportMeansIdentification]]
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TransportMeansIdentification] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.type" -> code))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[TransportMeansIdentification]]
      .map(_.head)
  }

  private def getCustomsOfficesForCountryAndRole(countryCode: String, role: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptySet[CustomsOffice]] = {
    val queryParams: Seq[(String, String)] = Seq(
      "data.countryId"  -> countryCode,
      "data.roles.role" -> role.toUpperCase.trim
    )
    getCustomsOffices(queryParams)
  }

  def getCustomsOfficeForId(id: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[CustomsOffice] = {
    val queryParams: Seq[(String, String)] = Seq("data.id" -> id)
    getCustomsOffices(queryParams).map(_.head)
  }

  def getCustomsOfficesForIds(ids: Seq[String])(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptySet[CustomsOffice]] = {
    val queryParams: Seq[(String, String)] = ids.map("data.id" -> _)
    getCustomsOffices(queryParams)
  }

  private def getCustomsOffices(queryParams: Seq[(String, String)])(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[NonEmptySet[CustomsOffice]] = {
    val url = url"${config.referenceDataUrl}/lists/CustomsOffices"
    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CustomsOffice]]
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail: _*)
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

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
