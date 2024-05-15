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
import config.Constants.DeclarationTypeSecurity.{EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import config.Constants.TransportModeCode.Air
import generated.CustomsOfficeOfTransitDeclaredType04
import generators.Generators
import models.reference.TransportMode.BorderMode
import models.reference.transport.border.active.Identification
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.BorderModeOfTransportPage
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import viewModels.ListItem
import viewModels.transport.border.active.AddAnotherBorderTransportViewModel.AddAnotherBorderTransportViewModelProvider

class AddAnotherBorderTransportViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one border means of transport" in {
      forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
        (mode, identification, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(Index(0)), identification)
            .setValue(IdentificationNumberPage(Index(0)), identificationNumber)

          val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)
          result.listItems.length mustBe 1
          result.title mustBe "You have added 1 border means of transport"
          result.heading mustBe "You have added 1 border means of transport"
          result.legend mustBe "Do you want to add another border means of transport?"
          result.hint mustBe "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustBe "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }

    "when there are multiple border means of transport" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString, Gen.choose(2, frontendAppConfig.maxActiveBorderTransports)) {
        (mode, identification, identificationNumber, activeBorderTransports) =>
          val userAnswers = (0 until activeBorderTransports).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              acc
                .setValue(IdentificationPage(Index(i)), identification)
                .setValue(IdentificationNumberPage(Index(i)), identificationNumber)
          }

          val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)
          result.listItems.length mustBe activeBorderTransports
          result.title mustBe s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.heading mustBe s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.legend mustBe "Do you want to add another border means of transport?"
          result.hint mustBe "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustBe "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }

    "with change and remove links" - {
      "for first Border Mode of transport when BorderMode of Transport is  5, " +
        "security type is NoSecurityDetails and CustomsOfficeOfTransitDeclared is not defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
            (mode, identification, identificationNumber) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }
    }

    "with change link and remove link  for each bordermode of transport when there are more than one" - {
      "for first Border Mode of transport when BorderMode of Transport is  not mail, " +
        "security type is NoSecurityDetails and CustomsOfficeOfTransitDeclared is not defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
            (mode, identification, identificationNumber) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }
      "for first Border Mode of transport when BorderMode of Transport is  5, " +
        "security type not NoSecurityDetails and CustomsOfficeOfTransitDeclared is not defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
            (mode, identification, identificationNumber) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = EntrySummaryDeclarationSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

      "for first Border Mode of transport when BorderMode of Transport is  5, " +
        "security type is not 1,2 or 3 and CustomsOfficeOfTransitDeclared is defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString, arbitrary[CustomsOfficeOfTransitDeclaredType04]) {
            (mode, identification, identificationNumber, customsOfficeOfTransit) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = EntrySummaryDeclarationSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Seq(customsOfficeOfTransit)
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

    }
    "with change link and no remove link  for each bordermode of transport when there is only one" - {
      "for first Border Mode of transport when BorderMode of Transport is  not mail, " +
        "security type is NoSecurityDetails and CustomsOfficeOfTransitDeclared is not defined" in {
          forAll(arbitrary[Identification], nonEmptyString) {
            (identification, identificationNumber) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = NoSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(BorderModeOfTransportPage, BorderMode(Air, "test"))

              val result = new AddAnotherBorderTransportViewModelProvider().apply(userAnswers, departureId, NormalMode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString}",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, NormalMode, Index(0)).url,
                  removeUrl = None
                )
              )
          }
        }
      "for first Border Mode of transport when BorderMode of Transport is  5, " +
        "security type not NoSecurityDetails and CustomsOfficeOfTransitDeclared is not defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString) {
            (mode, identification, identificationNumber) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = EntrySummaryDeclarationSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Nil
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

      "for first Border Mode of transport when BorderMode of Transport is  5, " +
        "security type is not 1,2 or 3 and CustomsOfficeOfTransitDeclared is defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification], nonEmptyString, arbitrary[CustomsOfficeOfTransitDeclaredType04]) {
            (mode, identification, identificationNumber, customsOfficeOfTransit) =>
              val userAnswers = emptyUserAnswers
                .copy(
                  departureData = basicIe015.copy(
                    TransitOperation = basicIe015.TransitOperation.copy(security = EntrySummaryDeclarationSecurityDetails),
                    CustomsOfficeOfTransitDeclared = Seq(customsOfficeOfTransit)
                  )
                )
                .setValue(IdentificationPage(Index(0)), identification)
                .setValue(IdentificationNumberPage(Index(0)), identificationNumber)
                .setValue(IdentificationPage(Index(1)), identification)
                .setValue(IdentificationNumberPage(Index(1)), identificationNumber)

              val result = new AddAnotherBorderTransportViewModelProvider()(userAnswers, departureId, mode)

              result.listItems mustBe Seq(
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(0)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(0)).url)
                ),
                ListItem(
                  name = s"${identification.asString} - $identificationNumber",
                  changeUrl = controllers.transport.border.active.routes.IdentificationController.onPageLoad(departureId, mode, Index(1)).url,
                  removeUrl = Some(controllers.transport.border.active.routes.RemoveBorderTransportYesNoController.onPageLoad(departureId, mode, Index(1)).url)
                )
              )
          }
        }

    }
  }
}
