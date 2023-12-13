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

import config.FrontendAppConfig
import models.reference.transport.border.active.Identification
import models.reference.{CustomsOffice, Nationality}
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import viewModels.Link

class ActiveBorderTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode,
  activeIndex: Index
)(implicit messages: Messages, appConfig: FrontendAppConfig)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def addBorderMeansOfTransportYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderMeansOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderMeansOfTransportYesNo",
    findValueInDepartureData = message => Option(message.Consignment.ActiveBorderTransportMeans.isDefined),
    id = Some("change-add-identification-for-the-border-means-of-transport")
  )

  def identificationType: Option[SummaryListRow] = getAnswerAndBuildRow[Identification](
    page = IdentificationPage(activeIndex),
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "transport.border.active.identification",
    findValueInDepartureData = _.Consignment.ActiveBorderTransportMeans
      .flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.typeOfIdentification).map(_.asIdentification)
      ), // TODO probably need to use ref data for this
    id = Some("change-identification")
  )

  def identificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IdentificationNumberPage(activeIndex),
    formatAnswer = formatAsText,
    prefix = "transport.border.active.identificationNumber",
    findValueInDepartureData = _.Consignment.ActiveBorderTransportMeans
      .flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.identificationNumber)
      ),
    id = Some("change-identification-number")
  )

  def nationality: Option[SummaryListRow] = getAnswerAndBuildRow[Nationality](
    page = NationalityPage(activeIndex),
    formatAnswer = _.toString.toText,
    prefix = "transport.border.active.nationality",
    findValueInDepartureData = _.Consignment.ActiveBorderTransportMeans
      .flatMap(
        seq =>
          seq
            .lift(activeIndex.position)
            .flatMap(_.nationality)
            .map(_.asNationality) // TODO probably need to use ref data for this
      ),
    id = Some("change-nationality")
  )

  def customsOffice: Option[SummaryListRow] = getAnswerAndBuildRow[CustomsOffice](
    page = CustomsOfficeActiveBorderPage(activeIndex),
    formatAnswer = formatAsText,
    prefix = "transport.border.active.customsOfficeActiveBorder",
    findValueInDepartureData = _.Consignment.ActiveBorderTransportMeans
      .flatMap(
        seq =>
          seq
            .lift(activeIndex.position)
            .flatMap(_.customsOfficeAtBorderReferenceNumber)
            .map(_.asCustomsOffice) // TODO probably need to use ref data for this
      ),
    id = Some("change-customs-office")
  )

  def conveyanceReferenceNumberYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddConveyanceReferenceYesNoPage(activeIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.active.addConveyanceReference",
    findValueInDepartureData = m =>
      (m.conveyanceRefNumberYesNoPresent,
       m.Consignment.ActiveBorderTransportMeans.flatMap(
         seq => seq.lift(activeIndex.position).map(_.conveyanceReferenceNumber.isDefined)
       )
      ) match {
        case (false, Some(true))  => Some(true)
        case (false, Some(false)) => Some(false)
        case _                    => None
      },
    id = Some("change-add-conveyance-reference-number")
  )

  def conveyanceReferenceNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ConveyanceReferenceNumberPage(activeIndex),
    formatAnswer = formatAsText,
    prefix = "transport.border.active.conveyanceReferenceNumber",
    findValueInDepartureData = _.Consignment.ActiveBorderTransportMeans
      .flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.conveyanceReferenceNumber)
      ),
    id = Some("change-conveyance-reference-number")
  )

  def addOrRemoveActiveBorderTransportsMeans(): Option[Link] =
    buildLink(BorderActiveListSection, userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isDefined) {
      Link(
        id = "add-or-remove-border-means-of-transport",
        text = messages("checkYourAnswers.transportMeans.addOrRemove"),
        href = controllers.transport.border.active.routes.AddAnotherBorderTransportController.onPageLoad(departureId, mode).url
      )
    }
}
