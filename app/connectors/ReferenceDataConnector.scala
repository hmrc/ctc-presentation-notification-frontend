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
import connectors.ReferenceDataConnector.*
import models.reference.*
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{LocationOfGoodsIdentification, LocationType}
import play.api.Logging
import play.api.cache.*
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2, cache: AsyncCacheApi) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Responses[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> "application/vnd.hmrc.2.0+json")
      .execute[Responses[T]]

  // https://www.playframework.com/documentation/2.6.x/ScalaCache#Accessing-the-Cache-API
  private def getOrElseUpdate[T: ClassTag](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Response[T]] =
    cache.getOrElseUpdate[Response[T]](url.toString, config.asyncCacheApiExpiration.seconds) {
      get[T](url).map(_.map(_.head))
    }

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/$listName"
    get[Country](url)
  }

  def getCountry(listName: String, code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Country]] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/$listName?$queryParameters"
    getOrElseUpdate[Country](url)
  }

  def getCustomsOfficesOfTransitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "TRA")

  def getCustomsOfficesOfDestinationForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "DES")

  def getCustomsOfficesOfExitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode.code, "EXT")

  def getCustomsOfficesOfDepartureForCountry(
    countryCode: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] =
    getCustomsOfficesForCountryAndRole(countryCode, "DEP")

  def getAddressPostcodeBasedCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Country]] =
    getCountries("CountryAddressPostcodeBased")

  def getCountriesWithoutZipCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[CountryCode]] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/CountryWithoutZip?$queryParameters"
    getOrElseUpdate[CountryCode](url)
  }

  def getUnLocodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[UnLocode]] = {
    val url = url"${config.referenceDataUrl}/lists/UnLocodeExtended"
    get[UnLocode](url)
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[UnLocode]] = {
    val queryParameters = Seq("data.unLocodeExtendedCode" -> unLocode)
    val url             = url"${config.referenceDataUrl}/lists/UnLocodeExtended?$queryParameters"
    getOrElseUpdate[UnLocode](url)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Nationality]] = {
    val url = url"${config.referenceDataUrl}/lists/Nationality"
    get[Nationality](url)
  }

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Nationality]] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/Nationality?$queryParameters"
    getOrElseUpdate[Nationality](url)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[SpecificCircumstanceIndicator]] = {
    val url = url"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    get[SpecificCircumstanceIndicator](url)
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationType]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    get[LocationType](url)
  }

  def getTypeOfLocation(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[LocationType]] = {
    val queryParameters = Seq("data.type" -> code)
    val url             = url"${config.referenceDataUrl}/lists/TypeOfLocation?$queryParameters"
    getOrElseUpdate[LocationType](url)
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationOfGoodsIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    get[LocationOfGoodsIdentification](url)
  }

  def getTransportModeCodes[T <: TransportMode[T]]()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[Responses[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    get[T](url)
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Identification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    get[Identification](url)
  }

  def getMeansOfTransportIdentificationTypeActive(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Identification]] = {
    val queryParameters = Seq("data.code" -> code)
    val url             = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive?$queryParameters"
    getOrElseUpdate[Identification](url)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[TransportMeansIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    get[TransportMeansIdentification](url)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[TransportMeansIdentification]] = {
    val queryParameters = Seq("data.type" -> code)
    val url             = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport?$queryParameters"
    getOrElseUpdate[TransportMeansIdentification](url)
  }

  private def getCustomsOfficesForCountryAndRole(
    countryCode: String,
    role: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val queryParameters: Seq[(String, String)] = Seq(
      "data.countryId"  -> countryCode,
      "data.roles.role" -> role.toUpperCase.trim
    )
    getCustomsOffices(queryParameters)
  }

  def getCustomsOfficeForId(
    id: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[CustomsOffice]] = {
    val queryParameters: Seq[(String, String)] = Seq("data.id" -> id)
    getCustomsOffices(queryParameters).map(_.map(_.head))
  }

  def getCustomsOfficesForIds(
    ids: Seq[String]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val queryParameters: Seq[(String, String)] = ids.map("data.id" -> _)
    getCustomsOffices(queryParameters)
  }

  private def getCustomsOffices(
    queryParameters: Seq[(String, String)]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val url = url"${config.referenceDataUrl}/lists/CustomsOffices?$queryParameters"
    get[CustomsOffice](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[Responses[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              Left(NoReferenceDataFoundException(url))
            case JsSuccess(head :: tail, _) =>
              Right(NonEmptySet.of(head, tail*))
            case JsError(errors) =>
              Left(JsResultException(errors))
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          Left(Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}"))
      }
}

object ReferenceDataConnector {
  type Responses[T] = Either[Exception, NonEmptySet[T]]
  type Response[T]  = Either[Exception, T]

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
