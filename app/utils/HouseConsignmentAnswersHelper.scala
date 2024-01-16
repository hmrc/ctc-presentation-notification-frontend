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

import models.reference.TransportMode.InlandMode
import models.reference.transport.border.active.Identification
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.reference.{CustomsOffice, Nationality}
import models.{Index, Mode, UserAnswers}
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import pages.houseConsignment.index.departureTransportMeans.{CountryPage, IdentificationNumberPage, IdentificationPage}
import pages.sections.houseConsignment.HouseConsignmentListSection
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.transport.InlandModePage
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class HouseConsignmentAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  cyaRefDataService: CheckYourAnswersReferenceDataService,
  mode: Mode,
  houseConsignmentIndex: Index
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  implicit val ua: UserAnswers = userAnswers


  def addDepartureTransportMeansYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddDepartureTransportMeansYesNoPage(houseConsignmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "houseConsignment.index.addDepartureTransportMeansYesNo",
    findValueInDepartureData = message => message.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).map(_.DepartureTransportMeans.isDefined),
    id = Some("change-add-departure-means-of-transport")
  )

  def identificationType(departureTransportMeansIndex:Index): Future[Option[SummaryListRow]] =
    fetchValue[TransportMeansIdentification](
      page = IdentificationPage(houseConsignmentIndex, departureTransportMeansIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).flatMap(_.DepartureTransportMeans.flatMap(
        seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.typeOfIdentification)
      )),
      refDataLookup = cyaRefDataService.getTransportMeansIdentification
    ).map {
      identification =>
        buildRowWithAnswer[TransportMeansIdentification](
          page = IdentificationPage(houseConsignmentIndex, departureTransportMeansIndex),
          optionalAnswer = identification,
          formatAnswer = formatDynamicEnumAsText(_),
          prefix = "houseConsignment.index.departureTransportMeans.identification",
          id = Some("change-identification"),
          args = houseConsignmentIndex.display
        )
    }

  def identificationNumber(departureTransportMeansIndex:Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IdentificationNumberPage(houseConsignmentIndex, departureTransportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "houseConsignment.index.departureTransportMeans.identificationNumber",
    findValueInDepartureData = _.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).flatMap(_.DepartureTransportMeans.flatMap(
      seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.identificationNumber)
    )),
    id = Some("change-identification-number"),
    args = houseConsignmentIndex.display
  )

  def nationality(departureTransportMeansIndex:Index): Future[Option[SummaryListRow]] =
    fetchValue[Nationality](
      page = CountryPage(houseConsignmentIndex, departureTransportMeansIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).flatMap(_.DepartureTransportMeans.flatMap(
        seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.nationality)
      )),
      refDataLookup = cyaRefDataService.getNationality
    ).map {
      nationality =>
        buildRowWithAnswer[Nationality](
          page = CountryPage(houseConsignmentIndex, departureTransportMeansIndex),
          optionalAnswer = nationality,
          formatAnswer = _.description.toText,
          prefix = "houseConsignment.index.departureTransportMeans.country",
          id = Some("change-country"),
          args = houseConsignmentIndex.display
        )
    }


  def getSection(): Future[Option[Section]] = {

    (userAnswers.get(InlandModePage), userAnswers.departureData.Consignment.inlandModeOfTransport, userAnswers.get(TransportMeansSection)) match {
      case (Some(InlandMode("5", _)), _, _) | (_, Some("5"), _) | (_, _, Some(_)) =>
        successful(None)
      case _ =>
        val sectionHC: Section =
          Section(
            sectionTitle = messages("checkYourAnswers.houseConsignment", houseConsignmentIndex.display),
            rows = Seq(addDepartureTransportMeansYesNo).flatten
          )

        val sectionDepartureTransportMeans: Seq[Section] =
          userAnswers.departureData.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).map(_.DepartureTransportMeans.zipWithIndex.map {
            case (_, i) =>
              Section(
                sectionTitle = messages("checkYourAnswers.departureTransportMeans", Index(i).display),
                rows = Seq(identificationType(Index(i)),
                  successful(identificationType(Index(i))),
                  nationality(Index(i))).flatten
              )
          }).getOrElse(Seq.empty)

      sectionHC +: sectionDepartureTransportMeans
    }
  }
}
