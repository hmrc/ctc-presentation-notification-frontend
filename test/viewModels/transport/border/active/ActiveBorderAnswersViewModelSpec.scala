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

package viewModels.transport.border.active

import base.SpecBase
import generated.CustomsOfficeOfTransitDeclaredType06
import generators.Generators
import models.reference.TransportMode.BorderMode
import models.reference.transport.border.active.Identification
import models.reference.{CustomsOffice, Nationality}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.border.active._
import viewModels.transport.border.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

class ActiveBorderAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mode: Mode                   = arbitrary[Mode].sample.value
  val identificationNumber: String = Gen.alphaNumStr.sample.value
  val conveyanceNumber: String     = Gen.alphaNumStr.sample.value

  "when active border transport exists in the 170" - {

    "when there is only one section " - {
      "when security type is either 1,2,3 and mode of transport is not air" - {
        "and add conveyance ref number is true" - {
          "and customs office of transit is not defined " - {
            "must return 6 rows and addAnotherLink is not defined" in {
              forAll(
                arbitrarySecurityDetailsNonZeroType.arbitrary,
                arbitrary[Identification],
                nonEmptyString,
                arbitrary[CustomsOffice],
                arbitrary[Nationality],
                nonEmptyString
              ) {
                (securityType, identification, identificationNumber, office, nationality, conveyanceRefNumber) =>
                  val answers = emptyUserAnswers
                    .copy(departureData =
                      basicIe015.copy(
                        TransitOperation = basicIe015.TransitOperation.copy(security = securityType),
                        CustomsOfficeOfTransitDeclared = Nil
                      )
                    )
                    .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
                    .setValue(IdentificationPage(activeIndex), identification)
                    .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                    .setValue(NationalityPage(activeIndex), nationality)
                    .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
                    .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                    .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)

                  val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
                  val activeBorderSection = viewModelProvider.apply(answers, departureId, mode, activeIndex).section

                  activeBorderSection.sectionTitle mustBe Some("Border means of transport 1")
                  activeBorderSection.sectionTitle mustBe defined
                  activeBorderSection.rows.size mustBe 6
                  activeBorderSection.addAnotherLink must not be defined
              }
            }
          }

          "and customs office of transit is  defined " - {
            "must return 6 rows and addAnotherLink is defined" in {
              forAll(
                arbitrarySecurityDetailsNonZeroType.arbitrary,
                arbitrary[Identification],
                nonEmptyString,
                arbitrary[CustomsOffice],
                arbitrary[Nationality],
                arbitrary[CustomsOfficeOfTransitDeclaredType06]
              ) {
                (securityType, identification, identificationNumber, office, nationality, officeOfTransit) =>
                  val answers = emptyUserAnswers
                    .copy(departureData =
                      basicIe015.copy(
                        Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                        TransitOperation = basicIe015.TransitOperation.copy(security = securityType),
                        CustomsOfficeOfTransitDeclared = Seq(officeOfTransit)
                      )
                    )
                    .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
                    .setValue(IdentificationPage(activeIndex), identification)
                    .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                    .setValue(NationalityPage(activeIndex), nationality)
                    .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
                    .setValue(AddConveyanceReferenceYesNoPage(activeIndex), true)
                    .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceNumber)

                  val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
                  val activeBorderSection = viewModelProvider.apply(answers, departureId, mode, activeIndex).section

                  activeBorderSection.sectionTitle mustBe Some("Border means of transport 1")
                  activeBorderSection.sectionTitle mustBe defined
                  activeBorderSection.rows.size mustBe 6
                  activeBorderSection.addAnotherLink mustBe defined
              }
            }
          }
        }

        "and add conveyance ref number is false" - {
          "and customs office of transit is not defined " - {
            "must return 5 rows and addAnotherLink is not defined" in {
              forAll(
                arbitrarySecurityDetailsNonZeroType.arbitrary,
                arbitrary[Identification],
                nonEmptyString,
                arbitrary[CustomsOffice],
                arbitrary[Nationality]
              ) {
                (securityType, identification, identificationNumber, office, nationality) =>
                  val answers = emptyUserAnswers
                    .copy(departureData =
                      basicIe015.copy(
                        Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                        TransitOperation = basicIe015.TransitOperation.copy(security = securityType),
                        CustomsOfficeOfTransitDeclared = Nil
                      )
                    )
                    .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
                    .setValue(IdentificationPage(activeIndex), identification)
                    .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                    .setValue(NationalityPage(activeIndex), nationality)
                    .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
                    .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)

                  val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
                  val activeBorderSection = viewModelProvider.apply(answers, departureId, mode, activeIndex).section

                  activeBorderSection.sectionTitle mustBe Some("Border means of transport 1")
                  activeBorderSection.sectionTitle mustBe defined
                  activeBorderSection.rows.size mustBe 5
                  activeBorderSection.addAnotherLink must not be defined
              }
            }
          }

          "and customs office of transit is  defined " - {
            "must return 5 rows and addAnotherLink is defined" in {
              forAll(
                arbitrarySecurityDetailsNonZeroType.arbitrary,
                arbitrary[Identification],
                nonEmptyString,
                arbitrary[CustomsOffice],
                arbitrary[Nationality],
                arbitrary[CustomsOfficeOfTransitDeclaredType06]
              ) {
                (securityType, identification, identificationNumber, office, nationality, officeOfTransit) =>
                  val answers = emptyUserAnswers
                    .copy(departureData =
                      basicIe015.copy(
                        Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                        TransitOperation = basicIe015.TransitOperation.copy(security = securityType),
                        CustomsOfficeOfTransitDeclared = Seq(officeOfTransit)
                      )
                    )
                    .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
                    .setValue(IdentificationPage(activeIndex), identification)
                    .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                    .setValue(NationalityPage(activeIndex), nationality)
                    .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
                    .setValue(AddConveyanceReferenceYesNoPage(activeIndex), false)

                  val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
                  val activeBorderSection = viewModelProvider.apply(answers, departureId, mode, activeIndex).section

                  activeBorderSection.sectionTitle mustBe Some("Border means of transport 1")
                  activeBorderSection.sectionTitle mustBe defined
                  activeBorderSection.rows.size mustBe 5
                  activeBorderSection.addAnotherLink mustBe defined
              }
            }
          }
        }
      }

      "when security type is either 1,2,3 and mode of transport is  air" - {
        "and customs office of transit is not defined " - {
          "must return 5 rows" in {
            forAll(
              arbitrarySecurityDetailsNonZeroType.arbitrary,
              arbitrary[Identification],
              nonEmptyString,
              arbitrary[CustomsOffice],
              arbitrary[Nationality],
              nonEmptyString
            ) {
              (securityType, identification, identificationNumber, office, nationality, conveyanceRefNumber) =>
                val answers = emptyUserAnswers
                  .copy(departureData =
                    basicIe015.copy(
                      Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                      TransitOperation = basicIe015.TransitOperation.copy(security = securityType),
                      CustomsOfficeOfTransitDeclared = Nil
                    )
                  )
                  .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
                  .setValue(IdentificationPage(activeIndex), identification)
                  .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
                  .setValue(NationalityPage(activeIndex), nationality)
                  .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
                  .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceRefNumber)

                val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
                val activeBorderSection = viewModelProvider.apply(answers, departureId, mode, activeIndex).section

                activeBorderSection.sectionTitle mustBe Some("Border means of transport 1")
                activeBorderSection.sectionTitle mustBe defined
                activeBorderSection.rows.size mustBe 5
                activeBorderSection.addAnotherLink must not be defined
            }
          }
        }
      }
    }

    "when there are 2 sections " - {

      "when ie15 customs office exists" - {

        "add another link should be defined in section 2 and not 1" in {
          forAll(
            arbitrary[Identification],
            arbitrary[Identification],
            arbitrary[CustomsOfficeOfTransitDeclaredType06]
          ) {
            (identification1, identification2, officeOfTransit) =>
              val answers = emptyUserAnswers
                .copy(departureData =
                  basicIe015.copy(
                    Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                    CustomsOfficeOfTransitDeclared = Seq(officeOfTransit)
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification1)
                .setValue(IdentificationPage(Index(1)), identification2)

              val viewModelProvider    = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
              val activeBorderSection1 = viewModelProvider.apply(answers, departureId, mode, Index(0)).section
              val activeBorderSection2 = viewModelProvider.apply(answers, departureId, mode, Index(1)).section

              activeBorderSection1.sectionTitle mustBe Some("Border means of transport 1")
              activeBorderSection1.sectionTitle mustBe defined
              activeBorderSection1.addAnotherLink must not be defined

              activeBorderSection2.sectionTitle mustBe Some("Border means of transport 2")
              activeBorderSection2.sectionTitle mustBe defined
              activeBorderSection2.addAnotherLink mustBe defined
          }
        }
      }

      "when ie15 customs office does not exist" - {

        "add another link should not be defined in section 2 and 1" in {
          forAll(
            arbitrary[Identification],
            arbitrary[Identification]
          ) {
            (identification1, identification2) =>
              val answers = emptyUserAnswers
                .copy(departureData =
                  basicIe015.copy(
                    Consignment = basicIe015.Consignment.copy(ActiveBorderTransportMeans = Nil),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification1)
                .setValue(IdentificationPage(Index(1)), identification2)

              val viewModelProvider    = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
              val activeBorderSection1 = viewModelProvider.apply(answers, departureId, mode, Index(0)).section
              val activeBorderSection2 = viewModelProvider.apply(answers, departureId, mode, Index(1)).section

              activeBorderSection1.sectionTitle mustBe Some("Border means of transport 1")
              activeBorderSection1.sectionTitle mustBe defined
              activeBorderSection1.addAnotherLink must not be defined

              activeBorderSection2.sectionTitle mustBe Some("Border means of transport 2")
              activeBorderSection2.sectionTitle mustBe defined
              activeBorderSection2.addAnotherLink must not be defined
          }
        }
      }
    }
  }
}
