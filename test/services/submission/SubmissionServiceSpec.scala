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

package services.submission

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import models.messages.{Address, HolderOfTheTransitProcedure}
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference._
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{Coordinates, DynamicAddress, Index, LocationOfGoodsIdentification, LocationType, PostalCodeAddress}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.sections.transport.equipment.EquipmentSection
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, InlandModePage, LimitDatePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.__
import scalaxb.XMLCalendar
import services.DateTimeService

import java.time.{LocalDate, LocalDateTime}

class SubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val service = app.injector.instanceOf[SubmissionService]

  private lazy val mockDateTimeService = mock[DateTimeService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DateTimeService].toInstance(mockDateTimeService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)

    when(mockDateTimeService.now)
      .thenReturn(LocalDateTime.of(2020, 1, 1, 9, 30, 0))
  }

  "messageSequence" - {
    "must create message sequence" - {
      "when GB office of departure" in {
        val result = service.messageSequence(eoriNumber, "GB00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.GB",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "CC170C"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC170C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }

      "when XI office of departure" in {
        val result = service.messageSequence(eoriNumber, "XI00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.XI",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "CC170C"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC170C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }
    }
  }

  "transitOperationReads" - {
    "must create transit operation" - {
      "when limit date defined" in {
        val userAnswers = emptyUserAnswers
          .setValue(LimitDatePage, LocalDate.of(2020, 1, 1))

        val reads  = service.transitOperationReads(userAnswers)
        val result = userAnswers.data.as[TransitOperationType24](reads)

        result mustBe TransitOperationType24(
          LRN = userAnswers.lrn,
          limitDate = Some(XMLCalendar("2020-01-01"))
        )
      }

      "when limit date undefined" in {
        val userAnswers = emptyUserAnswers

        val reads  = service.transitOperationReads(userAnswers)
        val result = userAnswers.data.as[TransitOperationType24](reads)

        result mustBe TransitOperationType24(
          LRN = userAnswers.lrn,
          limitDate = None
        )
      }
    }
  }

  "Consignment" - {

    "consignmentReads" - {
      import pages.locationOfGoods._
      import pages.transport.equipment._
      import pages.transport.equipment.index._
      import pages.transport.equipment.index.seals._

      "must return list of transport equipments" in {
        val userAnswers = emptyUserAnswers
          .setValue(ContainerIndicatorPage, true)
          .setValue(InlandModePage, InlandMode("im", ""))
          .setValue(BorderModeOfTransportPage, BorderMode("bm", ""))
          .setValue(LocationTypePage, LocationType("tol", ""))
          .setValue(LocationOfGoodsPage, LocationOfGoodsIdentification("qoi", ""))
          .setValue(ContainerIdentificationNumberPage(Index(0)), "cin1")
          .setValue(SealIdentificationNumberPage(Index(0), Index(0)), "sin11")
          .setValue(SealIdentificationNumberPage(Index(0), Index(1)), "sin12")
          .setValue(ItemPage(Index(0), Index(0)), Item(11, "id11"))
          .setValue(ItemPage(Index(0), Index(1)), Item(12, "id12"))
          .setValue(ContainerIdentificationNumberPage(Index(1)), "cin2")
          .setValue(SealIdentificationNumberPage(Index(1), Index(0)), "sin21")
          .setValue(SealIdentificationNumberPage(Index(1), Index(1)), "sin22")
          .setValue(ItemPage(Index(1), Index(0)), Item(21, "id21"))
          .setValue(ItemPage(Index(1), Index(1)), Item(22, "id22"))

        val reads  = service.consignmentReads
        val result = userAnswers.data.as[ConsignmentType08](reads)

        result.containerIndicator.value mustBe Number1

        result.inlandModeOfTransport.value mustBe "im"

        result.modeOfTransportAtTheBorder.value mustBe "bm"

        result.LocationOfGoods.typeOfLocation mustBe "tol"

        result.LocationOfGoods.qualifierOfIdentification mustBe "qoi"

        result.TransportEquipment mustBe Seq(
          TransportEquipmentType06(
            sequenceNumber = "1",
            containerIdentificationNumber = Some("cin1"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType05("1", "sin11"),
              SealType05("2", "sin12")
            ),
            GoodsReference = Seq(
              GoodsReferenceType02("1", 11),
              GoodsReferenceType02("2", 12)
            )
          ),
          TransportEquipmentType06(
            sequenceNumber = "2",
            containerIdentificationNumber = Some("cin2"),
            numberOfSeals = 2,
            Seal = Seq(
              SealType05("1", "sin21"),
              SealType05("2", "sin22")
            ),
            GoodsReference = Seq(
              GoodsReferenceType02("1", 21),
              GoodsReferenceType02("2", 22)
            )
          )
        )
      }
    }

    "transportEquipmentReads" - {
      import pages.transport.equipment._
      import pages.transport.equipment.index._
      import pages.transport.equipment.index.seals._

      "must return TransportEquipment when user answers exist" in {
        val userAnswers = emptyUserAnswers
          .setValue(ContainerIdentificationNumberPage(equipmentIndex), "containerIdentification")
          .setValue(SealIdentificationNumberPage(equipmentIndex, sealIndex), "sealIdentification")
          .setValue(ItemPage(equipmentIndex, itemIndex), Item(5, "itemDescription"))

        val reads  = service.transportEquipmentReads(equipmentIndex)
        val result = userAnswers.getValue(EquipmentSection(equipmentIndex)).as[TransportEquipmentType06](reads)

        result mustBe TransportEquipmentType06(
          sequenceNumber = equipmentIndex.sequenceNumber,
          containerIdentificationNumber = Some("containerIdentification"),
          numberOfSeals = 1,
          Seal = Seq(SealType05(sealIndex.sequenceNumber, "sealIdentification")),
          GoodsReference = Seq(GoodsReferenceType02(itemIndex.sequenceNumber, 5))
        )
      }
    }

    "locationOfGoodsReads" - {
      import pages.locationOfGoods._

      "must create location of goods" - {

        val locationType              = arbitrary[LocationType].sample.value
        val qualifierOfIdentification = arbitrary[LocationOfGoodsIdentification].sample.value
        val authorisationNumber       = Gen.option(Gen.alphaNumStr).sample.value
        val additionalIdentifier      = Gen.option(Gen.alphaNumStr).sample.value
        val unLocode                  = Gen.option(Gen.alphaNumStr).sample.value
        val customsOffice             = arbitrary[CustomsOffice].sample.value
        val coordinates               = arbitrary[Coordinates].sample.value
        val eori                      = Gen.alphaNumStr.sample.value
        val country                   = arbitrary[Country].sample.value
        val address                   = arbitrary[DynamicAddress].sample.value
        val postcodeAddress           = arbitrary[PostalCodeAddress].sample.value
        val name                      = Gen.alphaNumStr.sample.value
        val phoneNumber               = Gen.alphaNumStr.sample.value

        "when all options defined" in {
          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, locationType)
            .setValue(LocationOfGoodsPage, qualifierOfIdentification)
            .setValue(AuthorisationNumberPage, authorisationNumber)
            .setValue(AdditionalIdentifierPage, additionalIdentifier)
            .setValue(UnLocodePage, unLocode)
            .setValue(CustomsOfficeIdentifierPage, customsOffice)
            .setValue(CoordinatesPage, coordinates)
            .setValue(EoriPage, eori)
            .setValue(CountryPage, country)
            .setValue(AddressPage, address)
            .setValue(PostalCodePage, postcodeAddress)
            .setValue(contact.NamePage, name)
            .setValue(contact.PhoneNumberPage, phoneNumber)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType03](reads)

          result mustBe LocationOfGoodsType03(
            typeOfLocation = locationType.code,
            qualifierOfIdentification = qualifierOfIdentification.qualifier,
            authorisationNumber = authorisationNumber,
            additionalIdentifier = additionalIdentifier,
            UNLocode = unLocode,
            CustomsOffice = Some(
              CustomsOfficeType02(
                referenceNumber = customsOffice.id
              )
            ),
            GNSS = Some(
              GNSSType(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
              )
            ),
            EconomicOperator = Some(
              EconomicOperatorType03(
                identificationNumber = eori
              )
            ),
            Address = Some(
              AddressType14(
                streetAndNumber = address.numberAndStreet,
                postcode = address.postalCode,
                city = address.city,
                country = country.code.code
              )
            ),
            PostcodeAddress = Some(
              PostcodeAddressType02(
                houseNumber = Some(postcodeAddress.streetNumber),
                postcode = postcodeAddress.postalCode,
                country = postcodeAddress.country.code.code
              )
            ),
            ContactPerson = Some(
              ContactPersonType06(
                name = name,
                phoneNumber = phoneNumber,
                eMailAddress = None
              )
            )
          )
        }

        "when type of location not inferred" in {
          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, locationType)
            .setValue(LocationOfGoodsPage, qualifierOfIdentification)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType03](reads)

          result mustBe LocationOfGoodsType03(
            typeOfLocation = locationType.code,
            qualifierOfIdentification = qualifierOfIdentification.qualifier,
            authorisationNumber = None,
            additionalIdentifier = None,
            UNLocode = None,
            CustomsOffice = None,
            GNSS = None,
            EconomicOperator = None,
            Address = None,
            PostcodeAddress = None,
            ContactPerson = None
          )
        }

        "when type of location inferred" in {
          val userAnswers = emptyUserAnswers
            .setValue(InferredLocationTypePage, locationType)
            .setValue(LocationOfGoodsPage, qualifierOfIdentification)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType03](reads)

          result mustBe LocationOfGoodsType03(
            typeOfLocation = locationType.code,
            qualifierOfIdentification = qualifierOfIdentification.qualifier,
            authorisationNumber = None,
            additionalIdentifier = None,
            UNLocode = None,
            CustomsOffice = None,
            GNSS = None,
            EconomicOperator = None,
            Address = None,
            PostcodeAddress = None,
            ContactPerson = None
          )
        }
      }
    }

    "departureTransportMeansReads" - {
      import pages.transport.departureTransportMeans._

      "must create a departure transport means" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Nationality]) {
          (typeOfIdentification, identificationNumber, nationality) =>
            val userAnswers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage, typeOfIdentification)
              .setValue(TransportMeansIdentificationNumberPage, identificationNumber)
              .setValue(TransportMeansNationalityPage, nationality)

            val reads  = service.departureTransportMeansReads(Index(0))
            val result = userAnswers.getValue(TransportMeansSection).as[DepartureTransportMeansType05](reads)

            result mustBe DepartureTransportMeansType05(
              sequenceNumber = "1",
              typeOfIdentification = typeOfIdentification.code,
              identificationNumber = identificationNumber,
              nationality = nationality.code
            )
        }
      }
    }

    "placeOfLoading" - {
      import pages.loading._

      "must create PlaceOfLoading when user answers exist" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnLocodePage, "unLocode")
          .setValue(CountryPage, Country(CountryCode("IT"), "Italy"))
          .setValue(LocationPage, "location")

        val reads  = __.readNullableSafe[PlaceOfLoadingType03](service.placeOfLoadingReads)
        val result = userAnswers.data.as[Option[PlaceOfLoadingType03]](reads)

        result mustBe Some(
          PlaceOfLoadingType03(
            UNLocode = Some("unLocode"),
            country = Some("IT"),
            location = Some("location")
          )
        )
      }

      "must not create PlaceOfLoading when user answers don't exist" in {
        val userAnswers = emptyUserAnswers

        val reads  = __.readNullableSafe[PlaceOfLoadingType03](service.placeOfLoadingReads)
        val result = userAnswers.data.as[Option[PlaceOfLoadingType03]](reads)

        result mustBe Some(
          PlaceOfLoadingType03(
            UNLocode = None,
            country = None,
            location = None
          )
        )
      }
    }
  }

  "holderOfTransit" - {
    "must create holder of transit" - {
      "when Address defined" in {
        forAll(
          Gen.option(Gen.alphaNumStr),
          Gen.option(Gen.alphaNumStr),
          Gen.option(Gen.alphaNumStr),
          arbitrary[Address]
        ) {
          (identificationNumber, tirHolderIdentificationNumber, name, address) =>
            val holderOfTransit = HolderOfTheTransitProcedure(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              ContactPerson = None,
              Address = Some(address)
            )

            val result = service.holderOfTransit(holderOfTransit)

            result mustBe HolderOfTheTransitProcedureType19(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              Address = Some(
                AddressType17(
                  streetAndNumber = address.streetAndNumber,
                  postcode = address.postcode,
                  city = address.city,
                  country = address.country
                )
              )
            )
        }
      }

      "when Address undefined" in {
        forAll(
          Gen.option(Gen.alphaNumStr),
          Gen.option(Gen.alphaNumStr),
          Gen.option(Gen.alphaNumStr)
        ) {
          (identificationNumber, tirHolderIdentificationNumber, name) =>
            val holderOfTransit = HolderOfTheTransitProcedure(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              ContactPerson = None,
              Address = None
            )

            val result = service.holderOfTransit(holderOfTransit)

            result mustBe HolderOfTheTransitProcedureType19(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              Address = None
            )
        }
      }
    }
  }

  "representativeReads" - {
    import pages.representative._

    "when representative not present in user answers" - {
      "must not create representative" in {
        val userAnswers = emptyUserAnswers

        val reads  = __.readNullableSafe[RepresentativeType05](service.representativeReads)
        val result = userAnswers.data.as[Option[RepresentativeType05]](reads)

        result mustBe None
      }
    }

    "when representative present in user answers" - {
      "must create representative" - {
        "when contact person also present" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
            (eori, name, phoneNumber) =>
              val userAnswers = emptyUserAnswers
                .setValue(EoriPage, eori)
                .setValue(AddRepresentativeContactDetailsYesNoPage, true)
                .setValue(NamePage, name)
                .setValue(RepresentativePhoneNumberPage, phoneNumber)

              val reads  = __.readNullableSafe[RepresentativeType05](service.representativeReads)
              val result = userAnswers.data.as[Option[RepresentativeType05]](reads)

              result mustBe Some(
                RepresentativeType05(
                  identificationNumber = eori,
                  status = "2",
                  ContactPerson = Some(
                    ContactPersonType05(
                      name = name,
                      phoneNumber = phoneNumber,
                      eMailAddress = None
                    )
                  )
                )
              )
          }
        }

        "when contact person not present" in {
          forAll(Gen.alphaNumStr) {
            eori =>
              val userAnswers = emptyUserAnswers
                .setValue(EoriPage, eori)
                .setValue(AddRepresentativeContactDetailsYesNoPage, false)

              val reads  = __.readNullableSafe[RepresentativeType05](service.representativeReads)
              val result = userAnswers.data.as[Option[RepresentativeType05]](reads)

              result mustBe Some(
                RepresentativeType05(
                  identificationNumber = eori,
                  status = "2",
                  ContactPerson = None
                )
              )
          }
        }
      }
    }
  }
}
