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

package utils.transformer

import cats.data.OptionT
import config.Constants.QualifierOfTheIdentification.{AuthorisationNumberIdentifier, CustomsOfficeIdentifier, EoriNumberIdentifier}
import generated.LocationOfGoodsType04
import models.{RichAddressType06, RichCC015CType, RichGNSSType, UserAnswers}
import pages.locationOfGoods.*
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import services.{CountriesService, CustomsOfficesService, LocationOfGoodsIdentificationTypeService, LocationTypeService}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsTransformer @Inject() (
  locationOfGoodsIdentificationService: LocationOfGoodsIdentificationTypeService,
  locationTypeService: LocationTypeService,
  customsOfficeService: CustomsOfficesService,
  countryService: CountriesService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    locationOfGoods: Option[LocationOfGoodsType04]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    locationOfGoods.mapWithSets {
      value =>
        setLocationType(value) andThen
          setIdentification(value) andThen
          set(AuthorisationNumberPage, value.authorisationNumber) andThen
          setAddIdentifierYesNoPage(value) andThen
          set(AdditionalIdentifierPage, value.additionalIdentifier) andThen
          set(UnLocodePage, value.UNLocode) andThen
          set(CustomsOfficeIdentifierPage, value.CustomsOffice.map(_.referenceNumber), customsOfficeService.getCustomsOfficeById) andThen
          set(CoordinatesPage, value.GNSS.map(_.toCoordinates)) andThen
          set(EoriPage, value.EconomicOperator.map(_.identificationNumber)) andThen
          set(CountryPage, value.Address.map(_.country), countryService.getCountry) andThen
          set(AddressPage, value.Address.map(_.toDynamicAddress)) andThen
          setAddContactYesNoPage(value) andThen
          set(NamePage, value.ContactPerson.map(_.name)) andThen
          set(PhoneNumberPage, value.ContactPerson.map(_.phoneNumber))

    }

  private def setIdentification(locationOfGoods: LocationOfGoodsType04)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      (for {
        locationType        <- OptionT.fromOption[Future](getLocationType(userAnswers))
        identificationTypes <- OptionT.liftF(locationOfGoodsIdentificationService.getLocationOfGoodsIdentificationTypes(locationType))
      } yield identificationTypes match {
        case head :: Nil => set(InferredIdentificationPage, head).apply(userAnswers)
        case values      => set(IdentificationPage, values.find(_.code == locationOfGoods.qualifierOfIdentification)).apply(userAnswers)
      }).value.flatMap(_.getOrElse(Future.successful(userAnswers)))

  private def getLocationType(userAnswers: UserAnswers): Option[String] =
    userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation)

  private def setLocationType(locationOfGoods: LocationOfGoodsType04)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      (for {
        locationTypes <- OptionT.liftF(locationTypeService.getLocationTypes(userAnswers.departureData.isSimplified))
      } yield locationTypes match {
        case head :: Nil => set(InferredLocationTypePage, head).apply(userAnswers)
        case values      => set(LocationTypePage, values.find(_.code == locationOfGoods.typeOfLocation)).apply(userAnswers)
      }).value.flatMap(_.getOrElse(Future.successful(userAnswers)))

  private def setAddContactYesNoPage(locationOfGoods: LocationOfGoodsType04): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      if (userAnswers.get(IdentificationPage).map(_.code).contains(CustomsOfficeIdentifier)) {
        Future.successful(userAnswers)
      } else {
        set(AddContactYesNoPage, locationOfGoods.ContactPerson.isDefined).apply(userAnswers)
      }

  private def setAddIdentifierYesNoPage(locationOfGoods: LocationOfGoodsType04): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      val identifier = (userAnswers.get(IdentificationPage) orElse userAnswers.get(InferredIdentificationPage)).map(_.code)
      identifier match {
        case Some(EoriNumberIdentifier) | Some(AuthorisationNumberIdentifier) =>
          set(AddIdentifierYesNoPage, locationOfGoods.additionalIdentifier.isDefined).apply(userAnswers)
        case _ =>
          Future.successful(userAnswers)
      }
}
