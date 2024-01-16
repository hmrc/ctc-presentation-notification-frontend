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
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class ActiveBorderTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  cyaRefDataService: CheckYourAnswersReferenceDataService,
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

  def addBorderMeansOfTransportYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderMeansOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.border.addBorderMeansOfTransportYesNo",
    findValueInDepartureData = message => Option(message.Consignment.ActiveBorderTransportMeans.isDefined),
    id = Some("change-add-identification-for-the-border-means-of-transport")
  )

  def identificationType: Future[Option[SummaryListRow]] = {
    val gg = fetchValue[Identification](
      page = IdentificationPage(activeIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.typeOfIdentification)
      ),
      refDataLookup = cyaRefDataService.getBorderMeansIdentification
    )
    gg.map {
      identification =>
        buildRowWithAnswer[Identification](
          page = IdentificationPage(activeIndex),
          optionalAnswer = identification,
          formatAnswer = formatDynamicEnumAsText(_),
          prefix = "transport.border.active.identification",
          id = Some("change-identification")
        )
    }
  }

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

  def nationality: Future[Option[SummaryListRow]] =
    fetchValue[Nationality](
      page = NationalityPage(activeIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.nationality)
      ),
      refDataLookup = cyaRefDataService.getNationality
    ).map {
      nationality =>
        buildRowWithAnswer[Nationality](
          page = NationalityPage(activeIndex),
          optionalAnswer = nationality,
          formatAnswer = _.description.toText,
          prefix = "transport.border.active.nationality",
          id = Some("change-nationality")
        )
    }

  def customsOffice: Future[Option[SummaryListRow]] =
    fetchValue[CustomsOffice](
      page = CustomsOfficeActiveBorderPage(activeIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.ActiveBorderTransportMeans.flatMap(
        seq => seq.lift(activeIndex.position).flatMap(_.customsOfficeAtBorderReferenceNumber)
      ),
      refDataLookup = cyaRefDataService.getCustomsOffice
    ).map {
      customsOffice =>
        buildRowWithAnswer[CustomsOffice](
          page = CustomsOfficeActiveBorderPage(activeIndex),
          optionalAnswer = customsOffice,
          formatAnswer = formatAsText(_),
          prefix = "transport.border.active.customsOfficeActiveBorder",
          id = Some("change-customs-office")
        )
    }

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

  def getSection(): Future[Section] = {
    val identificationTypeFuture = identificationType
    val nationalityFuture        = nationality
    val customsOfficeFuture      = customsOffice
    for {
      addBorderMeansOfTransportYesNoRow <-
        if (userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isDefined) successful(addBorderMeansOfTransportYesNo) else successful(None)
      identificationTypeRow             <- identificationTypeFuture
      identificationNumberRow           <- successful(identificationNumber)
      nationalityRow                    <- nationalityFuture
      customsOfficeRow                  <- customsOfficeFuture
      conveyanceReferenceNumberYesNoRow <- successful(conveyanceReferenceNumberYesNo)
      conveyanceReferenceNumberRow      <- successful(conveyanceReferenceNumber)
    } yield {
      val rows = Seq(
        addBorderMeansOfTransportYesNoRow,
        identificationTypeRow,
        identificationNumberRow,
        nationalityRow,
        customsOfficeRow,
        conveyanceReferenceNumberYesNoRow,
        conveyanceReferenceNumberRow
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
}
