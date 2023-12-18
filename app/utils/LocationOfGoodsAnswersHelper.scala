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

import models.reference.{Country, CountryCode, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, Mode, PostalCodeAddress, UserAnswers}
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Empty
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

  def locationTypeRow(answer: String): Option[SummaryListRow] = getAnswerAndBuildRow[LocationType](
    page = LocationTypePage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.locationType",
    findValueInDepartureData = message =>
      message.Consignment.LocationOfGoods.map(
        x => LocationType(x.typeOfLocation, answer)
      ),
    id = Some("change-location-type")
  )

  def qualifierIdentificationRow(answer: String): Option[SummaryListRow] = getAnswerAndBuildRow[LocationOfGoodsIdentification](
    page = IdentificationPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.identification",
    findValueInDepartureData = message =>
      message.Consignment.LocationOfGoods.map(
        x => LocationOfGoodsIdentification(x.qualifierOfIdentification, answer)
      ),
    id = Some("change-qualifier-identification")
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
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.EconomicOperator.map(_.toString)),
    id = Some("change-eori")
  )

  def additionalIdentifierYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentifierYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addIdentifierYesNo",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.map(_.additionalIdentifier.isDefined),
    id = Some("change-add-additional-identifier")
  )

  def additionalIdentifierRow: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AdditionalIdentifierPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.additionalIdentifier",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.additionalIdentifier),
    id = Some("change-additional-identifier")
  )

  def unLocode: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = UnLocodePage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.unLocode",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.UNLocode),
    id = Some("change-unLocode")
  )

  def customsOfficeIdentifierTitleRow(): SummaryListRow = buildRowWithNoChangeLink(
    answer = Empty,
    prefix = "locationOfGoods.customsOfficeIdentifierTitle",
    args = Seq.empty
  )

  def customsOfficeIdentifierRow(answer: String): SummaryListRow = buildSimpleRow(
    answer = Text(answer),
    label = messages("locationOfGoods.customsOfficeIdentifier.checkYourAnswersLabel"),
    prefix = "locationOfGoods.customsOfficeIdentifier",
    id = Some("change-customs-office-identifier"),
    call = Some(controllers.locationOfGoods.routes.CustomsOfficeIdentifierController.onPageLoad(departureId, mode)),
    args = Seq.empty
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

  def countryTypeRow(answer: String): Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "locationOfGoods.country",
    findValueInDepartureData = message =>
      message.Consignment.LocationOfGoods.flatMap(
        x =>
          x.Address.map(
            x => Country(CountryCode(x.country), answer)
          )
      ),
    id = Some("change-location-of-goods-country")
  )

  def address: Option[SummaryListRow] = getAnswerAndBuildRow[DynamicAddress](
    page = AddressPage,
    formatAnswer = formatAsDynamicAddress,
    prefix = "locationOfGoods.address",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.Address.map(_.toDynamicAddress)),
    id = Some("change-location-of-goods-address")
  )

  def postCodeAddress: Option[SummaryListRow] = getAnswerAndBuildRow[PostalCodeAddress](
    page = PostalCodePage,
    formatAnswer = formatAsPostalCode,
    prefix = "locationOfGoods.postalCode",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.PostcodeAddress.map(_.toPostalCode)),
    id = Some("change-location-of-goods-postalCode")
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
        .flatMap(
          locationType => locationTypeRow(locationType.toString)
        ),
      userAnswers
        .get(IdentificationPage)
        .flatMap(
          qualifierIdentification => qualifierIdentificationRow(qualifierIdentification.toString)
        ),
      userAnswers
        .get(CountryPage)
        .flatMap(
          country => countryTypeRow(country.toString)
        ),
      authorisationNumber,
      address,
      postCodeAddress,
      eoriNumber,
      additionalIdentifierYesNo,
      additionalIdentifierRow,
      unLocode,
      userAnswers
        .get(CustomsOfficeIdentifierPage)
        .map(
          _ => customsOfficeIdentifierTitleRow()
        ),
      userAnswers
        .get(CustomsOfficeIdentifierPage)
        .map(
          customsOffice => customsOfficeIdentifierRow(customsOffice.toString)
        ),
      coordinates,
      locationOfGoodsContactYesNo,
      locationOfGoodsContactPersonName,
      locationOfGoodsContactPersonNumber
    ).flatten
    Future.successful(result)
  }

  private def buildFromDepartureData: Future[Seq[SummaryListRow]] =
    for {
      locationType <- fetchLocationTypeRow
      qualifierId  <- fetchQualifierIdentificationRow
      customsTitle = userAnswers.departureData.Consignment.LocationOfGoods
        .flatMap(_.CustomsOffice.map(_.referenceNumber))
        .map(
          _ => customsOfficeIdentifierTitleRow()
        )
      customsOffice <- fetchCustomsOfficeIdentifierRow
      countryType   <- fetchCountryTypeRow
    } yield Seq(
      locationType,
      qualifierId,
      authorisationNumber,
      unLocode,
      customsTitle,
      customsOffice,
      eoriNumber,
      coordinates,
      countryType,
      address,
      postCodeAddress,
      locationOfGoodsContactYesNo,
      locationOfGoodsContactPersonName,
      locationOfGoodsContactPersonNumber,
      additionalIdentifierYesNo,
      additionalIdentifierRow
    ).flatten

  def fetchCustomsOfficeIdentifierRow: Future[Option[SummaryListRow]] =
    fetchValue[CustomsOffice](
      checkYourAnswersReferenceDataService.getCustomsOffice(userAnswers.departureData.countryOfDeparture),
      userAnswers.departureData.Consignment.LocationOfGoods.flatMap(_.CustomsOffice.map(_.referenceNumber))
    ).map {
      _.map(
        customsOffice => customsOfficeIdentifierRow(customsOffice.toString)
      )
    }

  def fetchQualifierIdentificationRow: Future[Option[SummaryListRow]] =
    fetchValue[LocationOfGoodsIdentification](
      checkYourAnswersReferenceDataService.getQualifierOfIdentification,
      userAnswers.departureData.Consignment.LocationOfGoods.map(_.qualifierOfIdentification)
    ).map {
      _.flatMap(
        qualifierIdentification => qualifierIdentificationRow(qualifierIdentification.toString)
      )
    }

  private def fetchCountryTypeRow: Future[Option[SummaryListRow]] =
    fetchValue[Country](
      checkYourAnswersReferenceDataService.getCountry,
      userAnswers.departureData.Consignment.LocationOfGoods.flatMap(_.Address.map(_.country))
    ).map {
      _.flatMap(
        country => countryTypeRow(country.description)
      )
    }

  def fetchLocationTypeRow: Future[Option[SummaryListRow]] =
    fetchValue[LocationType](
      checkYourAnswersReferenceDataService.getLocationType,
      userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation)
    ).map {
      _.flatMap(
        locationType => locationTypeRow(locationType.toString)
      )
    }
}
