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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.*
import generators.Generators
import models.reference.{Country, CustomsOffice, LocationType as TypeOfLocation}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.*
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{CountriesService, CustomsOfficesService, LocationOfGoodsIdentificationTypeService, LocationTypeService}

import scala.concurrent.Future

class LocationOfGoodsTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[LocationOfGoodsTransformer]

  private lazy val mockLocationOfGoodsIdentificationTypeService = mock[LocationOfGoodsIdentificationTypeService]
  private lazy val mockLocationTypeService                      = mock[LocationTypeService]

  private val customsOfficeType02: CustomsOfficeType02 = CustomsOfficeType02("id")
  private val coordinates                              = arbitrary[GNSSType].sample.value
  private val eoriType02: EconomicOperatorType02       = EconomicOperatorType02(nonEmptyString.sample.value)
  private val addressType06: AddressType06             = arbitrary[AddressType06].sample.value
  private val contactPerson01                          = arbitrary[ContactPersonType01].sample.value
  private val country                                  = arbitrary[Country].sample.value

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[LocationOfGoodsIdentificationTypeService].toInstance(mockLocationOfGoodsIdentificationTypeService),
        bind[LocationTypeService].toInstance(mockLocationTypeService),
        bind[CustomsOfficesService].toInstance(mockCustomsOfficeService),
        bind[CountriesService].toInstance(mockCountriesService)
      )

  "must transform data " - {
    "when referenceData call returns  lists of one element" in {
      val locationOfGoodsIdentification = LocationOfGoodsIdentification("X", "description")

      val locationType = TypeOfLocation("code", "description")

      when(mockLocationTypeService.getLocationTypes(any())(any()))
        .thenReturn(Future.successful(Seq(locationType)))

      when(mockLocationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(any())(any()))
        .thenReturn(Future.successful(Seq(locationOfGoodsIdentification)))

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any()))
        .thenReturn(Future.successful(CustomsOffice("id", "name", Some("phoneNumber"))))

      when(mockCountriesService.getCountry(any())(any()))
        .thenReturn(Future.successful(country))

      forAll(
        arbitrary[LocationOfGoodsType04].map(
          _.copy(
            authorisationNumber = Some("value"),
            additionalIdentifier = Some("number"),
            UNLocode = Some("123"),
            CustomsOffice = Some(customsOfficeType02),
            GNSS = Some(coordinates),
            EconomicOperator = Some(eoriType02),
            Address = Some(addressType06.copy(country = country.code.code)),
            ContactPerson = Some(contactPerson01)
          )
        )
      ) {
        locationOfGoods =>
          val ie015 = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(
              LocationOfGoods = Some(locationOfGoods)
            )
          )

          val userAnswers = emptyUserAnswers.copy(departureData = ie015)

          val result = transformer.transform(Some(locationOfGoods)).apply(userAnswers).futureValue
          result.get(InferredIdentificationPage).value mustEqual locationOfGoodsIdentification
          result.get(InferredLocationTypePage).value mustEqual locationType
          result.get(AuthorisationNumberPage) mustEqual locationOfGoods.authorisationNumber
          result.getValue(AddIdentifierYesNoPage) mustEqual true
          result.get(AdditionalIdentifierPage) mustEqual locationOfGoods.additionalIdentifier
          result.get(UnLocodePage) mustEqual locationOfGoods.UNLocode
          result.getValue(CustomsOfficeIdentifierPage).id mustEqual customsOfficeType02.referenceNumber
          result.get(CoordinatesPage).value mustEqual Coordinates(coordinates.latitude, coordinates.longitude)
          result.get(EoriPage).value mustEqual eoriType02.identificationNumber
          result.get(AddressPage).value mustEqual DynamicAddress(addressType06.streetAndNumber, addressType06.city, addressType06.postcode)
          result.getValue(CountryPage) mustEqual country
          result.getValue(AddContactYesNoPage) mustEqual true
          result.getValue(NamePage) mustEqual contactPerson01.name
          result.getValue(PhoneNumberPage) mustEqual contactPerson01.phoneNumber
      }
    }

    "when reference data returns more than one element" in {
      val locationOfGoodsIdentification1 = LocationOfGoodsIdentification("V", "description1")
      val locationOfGoodsIdentification2 = LocationOfGoodsIdentification("code2", "description2")

      val locationType1 = TypeOfLocation("code1", "description1")
      val locationType2 = TypeOfLocation("code2", "description2")

      when(mockLocationTypeService.getLocationTypes(any())(any()))
        .thenReturn(Future.successful(Seq(locationType1, locationType2)))

      when(mockLocationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(any())(any()))
        .thenReturn(Future.successful(Seq(locationOfGoodsIdentification1, locationOfGoodsIdentification2)))

      when(mockCustomsOfficeService.getCustomsOfficeById(any())(any()))
        .thenReturn(Future.successful(CustomsOffice("id", "name", Some("phoneNumber"))))

      when(mockCountriesService.getCountry(any())(any()))
        .thenReturn(Future.successful(country))

      forAll(
        arbitrary[LocationOfGoodsType04].map(
          _.copy(
            qualifierOfIdentification = "V",
            typeOfLocation = "code2",
            authorisationNumber = Some("value"),
            additionalIdentifier = Some("number"),
            UNLocode = Some("123"),
            CustomsOffice = Some(customsOfficeType02),
            GNSS = Some(coordinates),
            EconomicOperator = Some(eoriType02),
            Address = Some(addressType06.copy(country = country.code.code)),
            ContactPerson = Some(contactPerson01)
          )
        )
      ) {
        locationOfGoods =>
          val ie015 = basicIe015.copy(
            Consignment = basicIe015.Consignment.copy(
              LocationOfGoods = Some(locationOfGoods)
            )
          )

          val userAnswers = emptyUserAnswers.copy(departureData = ie015)

          val result = transformer.transform(Some(locationOfGoods)).apply(userAnswers).futureValue
          result.getValue(IdentificationPage) mustEqual locationOfGoodsIdentification1
          result.getValue(LocationTypePage) mustEqual locationType2
          result.get(AuthorisationNumberPage) mustEqual locationOfGoods.authorisationNumber
          result.get(AddIdentifierYesNoPage) must not be defined
          result.get(AdditionalIdentifierPage) mustEqual locationOfGoods.additionalIdentifier
          result.get(UnLocodePage) mustEqual locationOfGoods.UNLocode
          result.getValue(CustomsOfficeIdentifierPage).id mustEqual customsOfficeType02.referenceNumber
          result.get(CoordinatesPage).value mustEqual Coordinates(coordinates.latitude, coordinates.longitude)
          result.get(EoriPage).value mustEqual eoriType02.identificationNumber
          result.get(AddressPage).value mustEqual DynamicAddress(addressType06.streetAndNumber, addressType06.city, addressType06.postcode)
          result.getValue(CountryPage) mustEqual country
          result.get(AddContactYesNoPage) must not be defined
          result.getValue(NamePage) mustEqual contactPerson01.name
          result.getValue(PhoneNumberPage) mustEqual contactPerson01.phoneNumber
      }
    }

  }
}
