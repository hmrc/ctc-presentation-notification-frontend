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

package utils

import models.{LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.locationOfGoods.{AddContactYesNoPage, AddIdentifierYesNoPage, AddressPage, AuthorisationNumberPage, EoriPage, IdentificationPage, LocationTypePage}
import models.{Coordinates, LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import pages.locationOfGoods._
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def locationTypeRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.locationType.checkYourAnswersLabel"),
    prefix = "locationOfGoods.locationType",
    id = Some("change-location-type"),
    call = Some(controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode)),
    args = Seq.empty
  )

  def qualifierIdentificationRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.identification.checkYourAnswersLabel"),
    prefix = "locationOfGoods.identification",
    id = Some("change-qualifier-identification"),
    call = Some(controllers.locationOfGoods.routes.IdentificationController.onPageLoad(departureId, mode)),
    args = Seq.empty
  )

  def authorisationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.authorisationNumber",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.authorisationNumber),
    id = Some("change-authorisation-number")
  )

  def eoriNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = EoriPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.eori",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.map(_.EconomicOperator.get.toString),
    id = Some("change-eori")
  )

  def additionalIdentifier: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentifierYesNoPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.additionalIdentifier",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.map(_.additionalIdentifier.isDefined),
    id = Some("change-additional-identifier")
  )

  def coordinates: Option[SummaryListRow] = getAnswerAndBuildRow[Coordinates](
    page = CoordinatesPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.coordinates",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.GNSS),
    id = Some("change-coordinates")
  )

  def locationOfGoodsContactYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddContactYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addContact",
    findValueInDepartureData = _.Consignment.LocationOfGoods.map(_.ContactPerson.isDefined),
    id = Some("change-add-contact")
  )

  def locationOfGoodsContactPersonName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = NamePage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.contact.name",
    findValueInDepartureData = _.Consignment.LocationOfGoods.flatMap(_.ContactPerson.map(_.name)),
    id = Some("change-person-name")
  )

  def locationOfGoodsContactPersonNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = PhoneNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.contactPhoneNumber",
    findValueInDepartureData = _.Consignment.LocationOfGoods.flatMap(_.ContactPerson.map(_.phoneNumber)),
    id = Some("change-person-number")
  )

  def locationOfGoodsSection: Future[Section] = {

    val isPresentInIE13orIE15 = userAnswers.departureData.Consignment.LocationOfGoods.isDefined

    val rows: Future[Seq[SummaryListRow]] = if (isPresentInIE13orIE15) {
      buildFromDepartureData
    } else { //IE170
      buildFromUserAnswers
    }

    rows.map {
      convertedRows =>
        Section(
          sectionTitle = messages("checkYourAnswers.locationOfGoods"),
          convertedRows
        )
    }
  }

  private def buildFromUserAnswers = {
    val result: Seq[SummaryListRow] = Seq(
      userAnswers
        .get(LocationTypePage)
        .map(
          locationType => locationTypeRow(locationType.toString)
        ),
      userAnswers
        .get(IdentificationPage)
        .map(
          qualifierIdentification => qualifierIdentificationRow(qualifierIdentification.toString)
        ),
      authorisationNumber,
      eoriNumber,
      additionalIdentifier,
      coordinates,
      locationOfGoodsContactYesNo,
      locationOfGoodsContactPersonName,
      locationOfGoodsContactPersonNumber
    ).flatten
    Future.successful(result)
  }

  private def buildFromDepartureData: Future[List[SummaryListRow]] = {
    val locationTypeRowOption = fetchValue[LocationType](
      checkYourAnswersReferenceDataService.getLocationType,
      userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation)
    ).map {
      _.map(
        locationType => locationTypeRow(locationType.toString)
      )
    }

    val qualifierIdentificationRowOption = fetchValue[LocationOfGoodsIdentification](
      checkYourAnswersReferenceDataService.getQualifierOfIdentification,
      userAnswers.departureData.Consignment.LocationOfGoods.map(_.qualifierOfIdentification)
    ).map {
      _.map(
        qualifierIdentification => qualifierIdentificationRow(qualifierIdentification.toString)
      )
    }

    Future
      .sequence(
        List(
          locationTypeRowOption,
          qualifierIdentificationRowOption,
          Future.successful(authorisationNumber),
          Future.successful(additionalIdentifier),
          Future.successful(eoriNumber),
          Future.successful(coordinates),
          Future.successful(locationOfGoodsContactYesNo),
          Future.successful(locationOfGoodsContactPersonName),
          Future.successful(locationOfGoodsContactPersonNumber)

        )
      )
      .map(_.flatten)
  }
}
