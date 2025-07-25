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
import generated.*
import generators.Generators
import models.reference.TransportMode.{BorderMode, InlandMode}
import models.reference.*
import models.reference.transport.border.active.Identification as ABTMIdentification
import models.reference.transport.transportMeans.TransportMeansIdentification as DTMIdentification
import models.{Coordinates, DynamicAddress, Index, LocationOfGoodsIdentification}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.houseConsignment.HouseConsignmentSection
import pages.sections.transport.border.BorderActiveSection
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.sections.transport.equipment.EquipmentSection
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.{ContainerIndicatorPage, InlandModePage, LimitDatePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.__
import play.api.test.Helpers.running
import scalaxb.XMLCalendar
import services.DateTimeService

import java.time.{LocalDate, LocalDateTime}

class SubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val service = app.injector.instanceOf[SubmissionService]

  private lazy val mockDateTimeService              = mock[DateTimeService]
  private lazy val mockMessageIdentificationService = mock[MessageIdentificationService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DateTimeService].toInstance(mockDateTimeService),
        bind[MessageIdentificationService].toInstance(mockMessageIdentificationService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)
    reset(mockMessageIdentificationService)

    when(mockDateTimeService.currentDateTime)
      .thenReturn(LocalDateTime.of(2020, 1, 1, 9, 30, 0))

    when(mockMessageIdentificationService.randomIdentifier)
      .thenReturn("foo")
  }

  "attributes" - {
    "must assign phase ID" - {
      "when phase6 disabled" in {
        running(phase5App) {
          app =>
            val service = app.injector.instanceOf[SubmissionService]
            val result  = service.attributes
            result.keys.size mustEqual 1
            result.get("@PhaseID").value.value.toString mustEqual "NCTS5.1"
        }
      }

      "when phase6 enabled" in {
        running(phase6App) {
          app =>
            val service = app.injector.instanceOf[SubmissionService]
            val result  = service.attributes
            result.keys.size mustEqual 1
            result.get("@PhaseID").value.value.toString mustEqual "NCTS6"
        }
      }
    }
  }

  "messageSequence" - {
    "must create message sequence" - {
      "when GB office of departure" in {
        val result = service.messageSequence(eoriNumber, "GB00001")

        result mustEqual MESSAGESequence(
          messageSender = eoriNumber.value,
          messageRecipient = "NTA.GB",
          preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
          messageIdentification = "foo",
          messageType = CC170C,
          correlationIdentifier = None
        )
      }

      "when XI office of departure" in {
        val result = service.messageSequence(eoriNumber, "XI00001")

        result mustEqual MESSAGESequence(
          messageSender = eoriNumber.value,
          messageRecipient = "NTA.XI",
          preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
          messageIdentification = "foo",
          messageType = CC170C,
          correlationIdentifier = None
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
        val result = userAnswers.data.as[TransitOperationType23](reads)

        result mustEqual TransitOperationType23(
          LRN = userAnswers.lrn,
          limitDate = Some(XMLCalendar("2020-01-01"))
        )
      }

      "when limit date undefined" in {
        val userAnswers = emptyUserAnswers

        val reads  = service.transitOperationReads(userAnswers)
        val result = userAnswers.data.as[TransitOperationType23](reads)

        result mustEqual TransitOperationType23(
          LRN = userAnswers.lrn,
          limitDate = None
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
          arbitrary[AddressType14]
        ) {
          (identificationNumber, tirHolderIdentificationNumber, name, address) =>
            val holderOfTransit = HolderOfTheTransitProcedureType23(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              ContactPerson = None,
              Address = Some(address)
            )

            val result = service.holderOfTransit(holderOfTransit)

            result mustEqual HolderOfTheTransitProcedureType13(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              Address = Some(
                AddressType14(
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
            val holderOfTransit = HolderOfTheTransitProcedureType23(
              identificationNumber = identificationNumber,
              TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
              name = name,
              ContactPerson = None,
              Address = None
            )

            val result = service.holderOfTransit(holderOfTransit)

            result mustEqual HolderOfTheTransitProcedureType13(
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

        val reads  = __.readNullableSafe[RepresentativeType06](service.representativeReads)
        val result = userAnswers.data.as[Option[RepresentativeType06]](reads)

        result must not be defined
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

              val reads  = __.readNullableSafe[RepresentativeType06](service.representativeReads)
              val result = userAnswers.data.as[Option[RepresentativeType06]](reads)

              result.value mustEqual
                RepresentativeType06(
                  identificationNumber = eori,
                  status = "2",
                  ContactPerson = Some(
                    ContactPersonType03(
                      name = name,
                      phoneNumber = phoneNumber,
                      eMailAddress = None
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

              val reads  = __.readNullableSafe[RepresentativeType06](service.representativeReads)
              val result = userAnswers.data.as[Option[RepresentativeType06]](reads)

              result.value mustEqual
                RepresentativeType06(
                  identificationNumber = eori,
                  status = "2",
                  ContactPerson = None
                )
          }
        }
      }
    }
  }

  "Consignment" - {

    "consignmentReads" - {
      import pages.houseConsignment.index.{departureTransportMeans => hcdtm}
      import pages.locationOfGoods._
      import pages.transport.border.{active => abtm}
      import pages.transport.equipment._
      import pages.transport.equipment.index._
      import pages.transport.equipment.index.seals._
      import pages.transport.{departureTransportMeans => dtm}
      import pages.{loading => pol}

      "must return consignment" - {
        "when house consignments non-empty" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, true)
            .setValue(InlandModePage, InlandMode("im", ""))
            .setValue(BorderModeOfTransportPage, BorderMode("bm", ""))
            .setValue(LocationTypePage, models.reference.LocationType("tol", ""))
            .setValue(IdentificationPage, LocationOfGoodsIdentification("qoi", ""))
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
            .setValue(dtm.TransportMeansIdentificationPage(Index(0)), DTMIdentification("dtmtoi1", ""))
            .setValue(dtm.TransportMeansIdentificationNumberPage(Index(0)), "dtmin1")
            .setValue(dtm.TransportMeansNationalityPage(Index(0)), Nationality("dtmn1", ""))
            .setValue(dtm.TransportMeansIdentificationPage(Index(1)), DTMIdentification("dtmtoi2", ""))
            .setValue(dtm.TransportMeansIdentificationNumberPage(Index(1)), "dtmin2")
            .setValue(dtm.TransportMeansNationalityPage(Index(1)), Nationality("dtmn2", ""))
            .setValue(abtm.CustomsOfficeActiveBorderPage(Index(0)), CustomsOffice("abtmcoabrn1", "", None))
            .setValue(abtm.IdentificationPage(Index(0)), ABTMIdentification("abtmtoi1", ""))
            .setValue(abtm.IdentificationNumberPage(Index(0)), "abtmin1")
            .setValue(abtm.NationalityPage(Index(0)), Nationality("abtmn1", ""))
            .setValue(abtm.ConveyanceReferenceNumberPage(Index(0)), "abtmcrn1")
            .setValue(abtm.CustomsOfficeActiveBorderPage(Index(1)), CustomsOffice("abtmcoabrn2", "", None))
            .setValue(abtm.IdentificationPage(Index(1)), ABTMIdentification("abtmtoi2", ""))
            .setValue(abtm.IdentificationNumberPage(Index(1)), "abtmin2")
            .setValue(abtm.NationalityPage(Index(1)), Nationality("abtmn2", ""))
            .setValue(abtm.ConveyanceReferenceNumberPage(Index(1)), "abtmcrn2")
            .setValue(pol.UnLocodePage, "polunl")
            .setValue(hcdtm.IdentificationPage(Index(0), Index(0)), DTMIdentification("dtmtoi11", ""))
            .setValue(hcdtm.IdentificationNumberPage(Index(0), Index(0)), "dtmin11")
            .setValue(hcdtm.CountryPage(Index(0), Index(0)), Nationality("dtmn11", ""))
            .setValue(hcdtm.IdentificationPage(Index(0), Index(1)), DTMIdentification("dtmtoi12", ""))
            .setValue(hcdtm.IdentificationNumberPage(Index(0), Index(1)), "dtmin12")
            .setValue(hcdtm.CountryPage(Index(0), Index(1)), Nationality("dtmn12", ""))
            .setValue(hcdtm.IdentificationPage(Index(1), Index(0)), DTMIdentification("dtmtoi21", ""))
            .setValue(hcdtm.IdentificationNumberPage(Index(1), Index(0)), "dtmin21")
            .setValue(hcdtm.CountryPage(Index(1), Index(0)), Nationality("dtmn21", ""))
            .setValue(hcdtm.IdentificationPage(Index(1), Index(1)), DTMIdentification("dtmtoi22", ""))
            .setValue(hcdtm.IdentificationNumberPage(Index(1), Index(1)), "dtmin22")
            .setValue(hcdtm.CountryPage(Index(1), Index(1)), Nationality("dtmn22", ""))

          val reads  = service.consignmentReads
          val result = userAnswers.data.as[ConsignmentType10](reads)

          result.containerIndicator.value mustEqual Number1

          result.inlandModeOfTransport.value mustEqual "im"

          result.modeOfTransportAtTheBorder.value mustEqual "bm"

          result.TransportEquipment mustEqual Seq(
            TransportEquipmentType03(
              sequenceNumber = 1,
              containerIdentificationNumber = Some("cin1"),
              numberOfSeals = 2,
              Seal = Seq(
                SealType01(1, "sin11"),
                SealType01(2, "sin12")
              ),
              GoodsReference = Seq(
                GoodsReferenceType01(1, 11),
                GoodsReferenceType01(2, 12)
              )
            ),
            TransportEquipmentType03(
              sequenceNumber = 2,
              containerIdentificationNumber = Some("cin2"),
              numberOfSeals = 2,
              Seal = Seq(
                SealType01(1, "sin21"),
                SealType01(2, "sin22")
              ),
              GoodsReference = Seq(
                GoodsReferenceType01(1, 21),
                GoodsReferenceType01(2, 22)
              )
            )
          )

          result.LocationOfGoods.typeOfLocation mustEqual "tol"

          result.LocationOfGoods.qualifierOfIdentification mustEqual "qoi"

          result.DepartureTransportMeans mustEqual Seq(
            DepartureTransportMeansType01(
              sequenceNumber = 1,
              typeOfIdentification = "dtmtoi1",
              identificationNumber = "dtmin1",
              nationality = "dtmn1"
            ),
            DepartureTransportMeansType01(
              sequenceNumber = 2,
              typeOfIdentification = "dtmtoi2",
              identificationNumber = "dtmin2",
              nationality = "dtmn2"
            )
          )

          result.ActiveBorderTransportMeans mustEqual Seq(
            ActiveBorderTransportMeansType03(
              sequenceNumber = 1,
              customsOfficeAtBorderReferenceNumber = "abtmcoabrn1",
              typeOfIdentification = "abtmtoi1",
              identificationNumber = "abtmin1",
              nationality = "abtmn1",
              conveyanceReferenceNumber = Some("abtmcrn1")
            ),
            ActiveBorderTransportMeansType03(
              sequenceNumber = 2,
              customsOfficeAtBorderReferenceNumber = "abtmcoabrn2",
              typeOfIdentification = "abtmtoi2",
              identificationNumber = "abtmin2",
              nationality = "abtmn2",
              conveyanceReferenceNumber = Some("abtmcrn2")
            )
          )

          result.PlaceOfLoading.value mustEqual
            PlaceOfLoadingType(
              UNLocode = Some("polunl"),
              country = None,
              location = None
            )

          result.HouseConsignment mustEqual Seq(
            HouseConsignmentType06(
              sequenceNumber = 1,
              DepartureTransportMeans = Seq(
                DepartureTransportMeansType01(
                  sequenceNumber = 1,
                  typeOfIdentification = "dtmtoi11",
                  identificationNumber = "dtmin11",
                  nationality = "dtmn11"
                ),
                DepartureTransportMeansType01(
                  sequenceNumber = 2,
                  typeOfIdentification = "dtmtoi12",
                  identificationNumber = "dtmin12",
                  nationality = "dtmn12"
                )
              )
            ),
            HouseConsignmentType06(
              sequenceNumber = 2,
              DepartureTransportMeans = Seq(
                DepartureTransportMeansType01(
                  sequenceNumber = 1,
                  typeOfIdentification = "dtmtoi21",
                  identificationNumber = "dtmin21",
                  nationality = "dtmn21"
                ),
                DepartureTransportMeansType01(
                  sequenceNumber = 2,
                  typeOfIdentification = "dtmtoi22",
                  identificationNumber = "dtmin22",
                  nationality = "dtmn22"
                )
              )
            )
          )
        }

        "when house consignments empty" in {
          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, models.reference.LocationType("tol", ""))
            .setValue(IdentificationPage, LocationOfGoodsIdentification("qoi", ""))

          val reads  = service.consignmentReads
          val result = userAnswers.data.as[ConsignmentType10](reads)

          result mustEqual ConsignmentType10(
            LocationOfGoods = LocationOfGoodsType02(
              typeOfLocation = "tol",
              qualifierOfIdentification = "qoi"
            ),
            HouseConsignment = Seq(
              HouseConsignmentType06(
                sequenceNumber = 1,
                DepartureTransportMeans = Nil
              )
            )
          )
        }
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
        val result = userAnswers.getValue(EquipmentSection(equipmentIndex)).as[TransportEquipmentType03](reads)

        result mustEqual TransportEquipmentType03(
          sequenceNumber = equipmentIndex.display,
          containerIdentificationNumber = Some("containerIdentification"),
          numberOfSeals = 1,
          Seal = Seq(SealType01(sealIndex.display, "sealIdentification")),
          GoodsReference = Seq(GoodsReferenceType01(itemIndex.display, 5))
        )
      }
    }

    "locationOfGoodsReads" - {
      import pages.locationOfGoods._

      "must create location of goods" - {

        val locationType              = arbitrary[models.reference.LocationType].sample.value
        val qualifierOfIdentification = arbitrary[LocationOfGoodsIdentification].sample.value
        val authorisationNumber       = Gen.option(Gen.alphaNumStr).sample.value
        val additionalIdentifier      = Gen.option(Gen.alphaNumStr).sample.value
        val unLocode                  = Gen.option(Gen.alphaNumStr).sample.value
        val customsOffice             = arbitrary[CustomsOffice].sample.value
        val coordinates               = arbitrary[Coordinates].sample.value
        val eori                      = Gen.alphaNumStr.sample.value
        val country                   = arbitrary[Country].sample.value
        val address                   = arbitrary[DynamicAddress].sample.value
        val name                      = Gen.alphaNumStr.sample.value
        val phoneNumber               = Gen.alphaNumStr.sample.value

        "when all options defined" in {
          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, locationType)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(AuthorisationNumberPage, authorisationNumber)
            .setValue(AdditionalIdentifierPage, additionalIdentifier)
            .setValue(UnLocodePage, unLocode)
            .setValue(CustomsOfficeIdentifierPage, customsOffice)
            .setValue(CoordinatesPage, coordinates)
            .setValue(EoriPage, eori)
            .setValue(CountryPage, country)
            .setValue(AddressPage, address)
            .setValue(contact.NamePage, name)
            .setValue(contact.PhoneNumberPage, phoneNumber)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType02](reads)

          result mustEqual LocationOfGoodsType02(
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
              EconomicOperatorType02(
                identificationNumber = eori
              )
            ),
            Address = Some(
              AddressType06(
                streetAndNumber = address.numberAndStreet,
                postcode = address.postalCode,
                city = address.city,
                country = country.code.code
              )
            ),
            ContactPerson = Some(
              ContactPersonType01(
                name = name,
                phoneNumber = phoneNumber,
                eMailAddress = None
              )
            )
          )
        }

        "when answers not inferred" in {
          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, locationType)
            .setValue(IdentificationPage, qualifierOfIdentification)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType02](reads)

          result mustEqual LocationOfGoodsType02(
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

        "when answers inferred" in {
          val userAnswers = emptyUserAnswers
            .setValue(InferredLocationTypePage, locationType)
            .setValue(InferredIdentificationPage, qualifierOfIdentification)

          val reads  = service.locationOfGoodsReads
          val result = userAnswers.data.as[LocationOfGoodsType02](reads)

          result mustEqual LocationOfGoodsType02(
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
      import models.reference.transport.transportMeans.TransportMeansIdentification
      import pages.transport.departureTransportMeans._

      "must create a departure transport means" in {
        forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr, arbitrary[Nationality]) {
          (typeOfIdentification, identificationNumber, nationality) =>
            val userAnswers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(transportIndex), typeOfIdentification)
              .setValue(TransportMeansIdentificationNumberPage(transportIndex), identificationNumber)
              .setValue(TransportMeansNationalityPage(transportIndex), nationality)

            val reads  = service.departureTransportMeansReads(transportIndex)
            val result = userAnswers.getValue(TransportMeansSection(transportIndex)).as[DepartureTransportMeansType01](reads)

            result mustEqual DepartureTransportMeansType01(
              sequenceNumber = 1,
              typeOfIdentification = typeOfIdentification.code,
              identificationNumber = identificationNumber,
              nationality = nationality.code
            )
        }
      }
    }

    "activeBorderTransportMeansReads" - {
      import models.reference.transport.border.active.Identification
      import pages.transport.border.active._

      "must create an active border transport means" in {
        forAll(arbitrary[CustomsOffice], arbitrary[Identification], Gen.alphaNumStr, arbitrary[Nationality], Gen.option(Gen.alphaNumStr)) {
          (customsOffice, typeOfIdentification, identificationNumber, nationality, conveyanceReferenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(CustomsOfficeActiveBorderPage(activeIndex), customsOffice)
              .setValue(IdentificationPage(activeIndex), typeOfIdentification)
              .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              .setValue(NationalityPage(activeIndex), nationality)
              .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceReferenceNumber)

            val reads  = service.activeBorderTransportMeansReads(activeIndex)
            val result = userAnswers.getValue(BorderActiveSection(activeIndex)).as[ActiveBorderTransportMeansType03](reads)

            result mustEqual ActiveBorderTransportMeansType03(
              sequenceNumber = 1,
              customsOfficeAtBorderReferenceNumber = customsOffice.id,
              typeOfIdentification = typeOfIdentification.code,
              identificationNumber = identificationNumber,
              nationality = nationality.code,
              conveyanceReferenceNumber = conveyanceReferenceNumber
            )
        }
      }
    }

    "placeOfLoadingReads" - {
      import pages.loading._

      "must create PlaceOfLoading when user answers exist" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnLocodePage, "unLocode")
          .setValue(CountryPage, Country(CountryCode("IT"), "Italy"))
          .setValue(LocationPage, "location")

        val reads  = __.readNullableSafe[PlaceOfLoadingType](service.placeOfLoadingReads)
        val result = userAnswers.data.as[Option[PlaceOfLoadingType]](reads)

        result.value mustEqual
          PlaceOfLoadingType(
            UNLocode = Some("unLocode"),
            country = Some("IT"),
            location = Some("location")
          )
      }

      "must not create PlaceOfLoading when user answers don't exist" in {
        val userAnswers = emptyUserAnswers

        val reads  = __.readNullableSafe[PlaceOfLoadingType](service.placeOfLoadingReads)
        val result = userAnswers.data.as[Option[PlaceOfLoadingType]](reads)

        result.value mustEqual
          PlaceOfLoadingType(
            UNLocode = None,
            country = None,
            location = None
          )
      }
    }

    "houseConsignmentReads" - {
      import models.reference.transport.transportMeans.TransportMeansIdentification
      import pages.houseConsignment.index.departureTransportMeans._

      "must create an active border transport means" in {
        forAll(
          arbitrary[TransportMeansIdentification],
          Gen.alphaNumStr,
          arbitrary[Nationality],
          arbitrary[TransportMeansIdentification],
          Gen.alphaNumStr,
          arbitrary[Nationality]
        ) {
          (typeOfIdentification1, identificationNumber1, nationality1, typeOfIdentification2, identificationNumber2, nationality2) =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(houseConsignmentIndex, Index(0)), typeOfIdentification1)
              .setValue(IdentificationNumberPage(houseConsignmentIndex, Index(0)), identificationNumber1)
              .setValue(CountryPage(houseConsignmentIndex, Index(0)), nationality1)
              .setValue(IdentificationPage(houseConsignmentIndex, Index(1)), typeOfIdentification2)
              .setValue(IdentificationNumberPage(houseConsignmentIndex, Index(1)), identificationNumber2)
              .setValue(CountryPage(houseConsignmentIndex, Index(1)), nationality2)

            val reads  = service.houseConsignmentReads(houseConsignmentIndex)
            val result = userAnswers.getValue(HouseConsignmentSection(houseConsignmentIndex)).as[HouseConsignmentType06](reads)

            result mustEqual HouseConsignmentType06(
              sequenceNumber = 1,
              DepartureTransportMeans = Seq(
                DepartureTransportMeansType01(
                  sequenceNumber = 1,
                  typeOfIdentification = typeOfIdentification1.code,
                  identificationNumber = identificationNumber1,
                  nationality = nationality1.code
                ),
                DepartureTransportMeansType01(
                  sequenceNumber = 2,
                  typeOfIdentification = typeOfIdentification2.code,
                  identificationNumber = identificationNumber2,
                  nationality = nationality2.code
                )
              )
            )
        }
      }
    }
  }
}
