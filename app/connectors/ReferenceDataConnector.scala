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
import models.LocationOfGoodsIdentification
import models.reference.*
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
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
      .setHeader(HeaderNames.Accept -> {
        val version = if (config.isPhase6Enabled) "2.0" else "1.0"
        s"application/vnd.hmrc.$version+json"
      })
      .execute[Responses[T]]

  // https://www.playframework.com/documentation/2.6.x/ScalaCache#Accessing-the-Cache-API
  private def getOrElseUpdate[T: ClassTag](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Response[T]] =
    cache.getOrElseUpdate[Response[T]](url.toString, config.asyncCacheApiExpiration.seconds) {
      get[T](url).map(_.map(_.head))
    }

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Country]] = {
    val url                            = url"${config.referenceDataUrl}/lists/$listName"
    implicit val reads: Reads[Country] = Country.reads(config)
    get[Country](url)
  }

  def getCountry(listName: String, code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Country]] = {
    val queryParameters                = Country.queryParams(code)(config)
    implicit val reads: Reads[Country] = Country.reads(config)
    val url                            = url"${config.referenceDataUrl}/lists/$listName?$queryParameters"
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
    val queryParameters                    = CountryCode.queryParams(code)(config)
    implicit val reads: Reads[CountryCode] = CountryCode.reads(config)
    val url                                = url"${config.referenceDataUrl}/lists/CountryWithoutZip?$queryParameters"
    getOrElseUpdate[CountryCode](url)
  }

  def getUnLocodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[UnLocode]] = {
    implicit val reads: Reads[UnLocode] = UnLocode.reads(config)
    val url                             = url"${config.referenceDataUrl}/lists/UnLocodeExtended"
    get[UnLocode](url)
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[UnLocode]] = {
    val queryParameters                 = UnLocode.queryParams(unLocode)(config)
    implicit val reads: Reads[UnLocode] = UnLocode.reads(config)
    val url                             = url"${config.referenceDataUrl}/lists/UnLocodeExtended?$queryParameters"
    getOrElseUpdate[UnLocode](url)
  }

  def getNationalities()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Nationality]] = {
    implicit val reads: Reads[Nationality] = Nationality.reads(config)
    val url                                = url"${config.referenceDataUrl}/lists/Nationality"
    get[Nationality](url)
  }

  def getNationality(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Nationality]] = {
    val queryParameters                    = Nationality.queryParams(code)(config)
    implicit val reads: Reads[Nationality] = Nationality.reads(config)
    val url                                = url"${config.referenceDataUrl}/lists/Nationality?$queryParameters"
    getOrElseUpdate[Nationality](url)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[SpecificCircumstanceIndicator]] = {
    implicit val reads: Reads[SpecificCircumstanceIndicator] = SpecificCircumstanceIndicator.reads(config)
    val url                                                  = url"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    get[SpecificCircumstanceIndicator](url)
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationType]] = {
    implicit val reads: Reads[LocationType] = LocationType.reads(config)
    val url                                 = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    get[LocationType](url)
  }

  def getTypeOfLocation(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[LocationType]] = {
    val queryParameters                     = LocationType.queryParams(code)(config)
    implicit val reads: Reads[LocationType] = LocationType.reads(config)
    val url                                 = url"${config.referenceDataUrl}/lists/TypeOfLocation?$queryParameters"
    getOrElseUpdate[LocationType](url)
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationOfGoodsIdentification]] = {
    val url                                                  = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    implicit val reads: Reads[LocationOfGoodsIdentification] = LocationOfGoodsIdentification.reads(config)
    get[LocationOfGoodsIdentification](url)
  }

  private def getTransportModeCodes[T <: TransportMode[T]]()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    rds: Reads[T],
    order: Order[T]
  ): Future[Responses[T]] = {
    val url = url"${config.referenceDataUrl}/lists/TransportModeCode"
    get[T](url)
  }

  def getBorderModes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[BorderMode]] = {
    implicit val reads: Reads[BorderMode] = BorderMode.reads(config)
    getTransportModeCodes()
  }

  def getInlandModes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[InlandMode]] = {
    implicit val reads: Reads[InlandMode] = InlandMode.reads(config)
    getTransportModeCodes()
  }

  def getMeansOfTransportIdentificationTypesActive()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Identification]] = {
    implicit val reads: Reads[Identification] = Identification.reads(config)
    val url                                   = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive"
    get[Identification](url)
  }

  def getMeansOfTransportIdentificationTypeActive(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Identification]] = {
    val queryParameters                       = Identification.queryParams(code)(config)
    implicit val reads: Reads[Identification] = Identification.reads(config)
    val url                                   = url"${config.referenceDataUrl}/lists/TypeOfIdentificationofMeansOfTransportActive?$queryParameters"
    getOrElseUpdate[Identification](url)
  }

  def getMeansOfTransportIdentificationTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[TransportMeansIdentification]] = {
    val url                                                 = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport"
    implicit val reads: Reads[TransportMeansIdentification] = TransportMeansIdentification.reads(config)
    get[TransportMeansIdentification](url)
  }

  def getMeansOfTransportIdentificationType(
    code: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[TransportMeansIdentification]] = {
    val queryParameters                                     = TransportMeansIdentification.queryParams(code)(config)
    implicit val reads: Reads[TransportMeansIdentification] = TransportMeansIdentification.reads(config)
    val url                                                 = url"${config.referenceDataUrl}/lists/TypeOfIdentificationOfMeansOfTransport?$queryParameters"
    getOrElseUpdate[TransportMeansIdentification](url)
  }

  private def getCustomsOfficesForCountryAndRole(
    countryCode: String,
    role: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val queryParameters = CustomsOffice.queryParameters(countryCodes = Seq(countryCode), roles = Seq(role))(config)
    getCustomsOffices(queryParameters)
  }

  def getCustomsOfficeForId(
    id: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[CustomsOffice]] = {
    val queryParameters = CustomsOffice.queryParameters(ids = Seq(id))(config)
    getCustomsOffices(queryParameters).map(_.map(_.head))
  }

  def getCustomsOfficesForIds(
    ids: Seq[String]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val queryParameters = CustomsOffice.queryParameters(ids = ids)(config)
    getCustomsOffices(queryParameters)
  }

  private def getCustomsOffices(
    queryParameters: Seq[(String, String)]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    val url                                        = url"${config.referenceDataUrl}/lists/CustomsOffices?$queryParameters"
    implicit val reads: Reads[List[CustomsOffice]] = CustomsOffice.listReads(config)
    get[CustomsOffice](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[Responses[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          val json = if (config.isPhase6Enabled) response.json else response.json \ "data"
          json.validate[List[A]] match {
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
