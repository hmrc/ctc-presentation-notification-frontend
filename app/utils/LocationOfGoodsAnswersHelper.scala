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

import models.reference.{Country, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, Mode, PostalCodeAddress, UserAnswers}
import pages.locationOfGoods._
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.Section

class LocationOfGoodsAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def locationTypeRow: Option[SummaryListRow] = buildRowWithAnswer[LocationType](
    page = LocationTypePage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.locationType",
    id = Some("change-location-type")
  )

  def qualifierIdentificationRow: Option[SummaryListRow] = buildRowWithAnswer[LocationOfGoodsIdentification](
    page = IdentificationPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.identification",
    id = Some("change-qualifier-identification")
  )

  def authorisationNumber: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.authorisationNumber",
    id = Some("change-authorisation-number")
  )

  def eoriNumber: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = EoriPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.eori",
    id = Some("change-eori")
  )

  def additionalIdentifierYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddIdentifierYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addIdentifierYesNo",
    id = Some("change-add-additional-identifier")
  )

  def additionalIdentifierRow: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = AdditionalIdentifierPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.additionalIdentifier",
    id = Some("change-additional-identifier")
  )

  def unLocode: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = UnLocodePage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.unLocode",
    id = Some("change-unLocode")
  )

  def customsOfficeIdentifierRow: Option[SummaryListRow] = buildRowWithAnswer[CustomsOffice](
    page = CustomsOfficeIdentifierPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.customsOfficeIdentifier",
    id = Some("change-customs-office-identifier")
  )

  def coordinates: Option[SummaryListRow] = buildRowWithAnswer[Coordinates](
    page = CoordinatesPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.coordinates",
    id = Some("change-coordinates")
  )

  def locationOfGoodsContactYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddContactYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addContact",
    id = Some("change-add-contact")
  )

  def locationOfGoodsContactPersonName: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = NamePage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.contact.name",
    id = Some("change-person-name")
  )

  def locationOfGoodsContactPersonNumber: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = PhoneNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.contactPhoneNumber",
    id = Some("change-person-number")
  )

  def countryTypeRow: Option[SummaryListRow] = buildRowWithAnswer[Country](
    page = CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "locationOfGoods.country",
    id = Some("change-location-of-goods-country")
  )

  def address: Option[SummaryListRow] = buildRowWithAnswer[DynamicAddress](
    page = AddressPage,
    formatAnswer = formatAsDynamicAddress,
    prefix = "locationOfGoods.address",
    id = Some("change-location-of-goods-address")
  )

  def postCodeAddress: Option[SummaryListRow] = buildRowWithAnswer[PostalCodeAddress](
    page = PostalCodePage,
    formatAnswer = formatAsPostalCode,
    prefix = "locationOfGoods.postalCode",
    id = Some("change-location-of-goods-postalCode")
  )

  def locationOfGoodsSection: Section = {
    val rows: Seq[SummaryListRow] = Seq(
      locationTypeRow,
      qualifierIdentificationRow,
      authorisationNumber,
      unLocode,
      customsOfficeIdentifierRow,
      eoriNumber,
      coordinates,
      countryTypeRow,
      address,
      postCodeAddress,
      locationOfGoodsContactYesNo,
      locationOfGoodsContactPersonName,
      locationOfGoodsContactPersonNumber,
      if (authorisationNumber.isDefined | eoriNumber.isDefined) additionalIdentifierYesNo else None,
      additionalIdentifierRow
    ).flatten

    Section(
      sectionTitle = messages("checkYourAnswers.locationOfGoods"),
      rows = rows
    )

  }

}
