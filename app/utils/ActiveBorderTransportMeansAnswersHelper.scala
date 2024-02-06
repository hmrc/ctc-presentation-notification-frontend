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

import models.reference.transport.border.active.Identification
import models.reference.{CustomsOffice, Nationality}
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.ExecutionContext

class ActiveBorderTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode,
  activeIndex: Index
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  implicit val ua: UserAnswers = userAnswers

  private val lastIndex = Index(
    userAnswers
      .get(BorderActiveListSection)
      .map(_.value.length - 1)
      .getOrElse(userAnswers.departureData.Consignment.ActiveBorderTransportMeans.map(_.length - 1).getOrElse(0))
  )

  def addBorderMeansOfTransportYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddBorderMeansOfTransportYesNoPage,
    optionalAnswer = userAnswers.get(AddBorderMeansOfTransportYesNoPage),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderMeansOfTransportYesNo",
    id = Some(s"change-add-identification-for-the-border-means-of-transport")
  )

  def identificationType: Option[SummaryListRow] = buildRowWithAnswer[Identification](
    page = IdentificationPage(activeIndex),
    optionalAnswer = userAnswers.get(IdentificationPage(activeIndex)),
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "transport.border.active.identification",
    id = Some(s"change-identification-${activeIndex.display}")
  )

  def identificationNumber: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = IdentificationNumberPage(activeIndex),
    optionalAnswer = userAnswers.get(IdentificationNumberPage(activeIndex)),
    formatAnswer = formatAsText,
    prefix = "transport.border.active.identificationNumber",
    id = Some(s"change-identification-number-${activeIndex.display}")
  )

  def nationality: Option[SummaryListRow] = buildRowWithAnswer[Nationality](
    page = NationalityPage(activeIndex),
    optionalAnswer = userAnswers.get(NationalityPage(activeIndex)),
    formatAnswer = _.description.toText,
    prefix = "transport.border.active.nationality",
    id = Some(s"change-nationality-${activeIndex.display}")
  )

  def customsOffice: Option[SummaryListRow] = buildRowWithAnswer[CustomsOffice](
    page = CustomsOfficeActiveBorderPage(activeIndex),
    optionalAnswer = userAnswers.get(CustomsOfficeActiveBorderPage(activeIndex)),
    formatAnswer = formatAsText(_),
    prefix = "transport.border.active.customsOfficeActiveBorder",
    id = Some(s"change-customs-office-${activeIndex.display}")
  )

  def conveyanceReferenceNumberYesNo: Option[SummaryListRow] = buildRowWithAnswer[Boolean](
    page = AddConveyanceReferenceYesNoPage(activeIndex),
    optionalAnswer = userAnswers.get(AddConveyanceReferenceYesNoPage(activeIndex)),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.active.addConveyanceReference",
    id = Some(s"change-add-conveyance-reference-number-${activeIndex.display}")
  )

  def conveyanceReferenceNumber: Option[SummaryListRow] = buildRowWithAnswer[String](
    page = ConveyanceReferenceNumberPage(activeIndex),
    optionalAnswer = userAnswers.get(ConveyanceReferenceNumberPage(activeIndex)),
    formatAnswer = formatAsText,
    prefix = "transport.border.active.conveyanceReferenceNumber",
    id = Some(s"change-conveyance-reference-number-${activeIndex.display}")
  )

  def addOrRemoveActiveBorderTransportsMeans(): Option[Link] =
    buildLink(BorderActiveListSection, userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isDefined) {
      Link(
        id = "add-or-remove-border-means-of-transport",
        text = messages("checkYourAnswers.transportMeans.addOrRemove"),
        href = controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode).url
      )
    }

  def getSection(): Section = {
    val rows = Seq(
      if (userAnswers.get(AddBorderMeansOfTransportYesNoPage).isDefined) addBorderMeansOfTransportYesNo else None,
      identificationType,
      identificationNumber,
      nationality,
      customsOffice,
      conveyanceReferenceNumberYesNo,
      conveyanceReferenceNumber
    ).flatten

    Section(
      sectionTitle = messages("checkYourAnswers.transportMeans.active.withIndex", activeIndex.display),
      rows = rows,
      addAnotherLink = (userAnswers.departureData.CustomsOfficeOfTransitDeclared, lastIndex == activeIndex) match {
        case (Some(_), true) => addOrRemoveActiveBorderTransportsMeans()
        case _               => None
      }
    )
  }
}
