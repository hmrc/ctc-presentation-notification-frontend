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
import scala.util.{Failure, Success}

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
    id = Some("change-add-departure-means-of-transport"),
    args = houseConsignmentIndex.display
  )

  def identificationType(departureTransportMeansIndex: Index): Future[Option[SummaryListRow]] =
    fetchValue[TransportMeansIdentification](
      page = IdentificationPage(houseConsignmentIndex, departureTransportMeansIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.HouseConsignment
        .lift(houseConsignmentIndex.position)
        .flatMap(
          _.DepartureTransportMeans.flatMap(
            seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.typeOfIdentification)
          )
        ),
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

  def identificationNumber(departureTransportMeansIndex: Index): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = IdentificationNumberPage(houseConsignmentIndex, departureTransportMeansIndex),
    formatAnswer = formatAsText,
    prefix = "houseConsignment.index.departureTransportMeans.identificationNumber",
    findValueInDepartureData = _.Consignment.HouseConsignment
      .lift(houseConsignmentIndex.position)
      .flatMap(
        _.DepartureTransportMeans.flatMap(
          seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.identificationNumber)
        )
      ),
    id = Some("change-identification-number"),
    args = houseConsignmentIndex.display
  )

  def nationality(departureTransportMeansIndex: Index): Future[Option[SummaryListRow]] =
    fetchValue[Nationality](
      page = CountryPage(houseConsignmentIndex, departureTransportMeansIndex),
      valueFromDepartureData = userAnswers.departureData.Consignment.HouseConsignment
        .lift(houseConsignmentIndex.position)
        .flatMap(
          _.DepartureTransportMeans.flatMap(
            seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.nationality)
          )
        ),
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

  def getSection(): Future[Seq[Section]] = {

    val sectionHC: Future[Seq[Section]] = successful(
      Section(
        sectionTitle = messages("checkYourAnswers.houseConsignment", houseConsignmentIndex.display),
        rows = Seq(addDepartureTransportMeansYesNo).flatten
      ).toSeq
    )

    val sectionDepartureTransportMeans: Future[Seq[Section]] =
      userAnswers.departureData.Consignment.HouseConsignment
        .lift(houseConsignmentIndex.position)
        .map {
          consignment =>
            consignment.DepartureTransportMeans
              .map {
                departureTransportMeans =>
                  val futureSections: Seq[Future[Section]] = departureTransportMeans.zipWithIndex.map {
                    case (_, i) =>
                      val futureSection: Future[Section] = for {
                        identificationTypeRow   <- identificationType(Index(i))
                        identificationNumberRow <- successful(identificationNumber(Index(i)))
                        nationalityRow          <- nationality(Index(i))
                      } yield Section(
                        sectionTitle = messages("checkYourAnswers.departureTransportMeansWithIndex", Index(i).display),
                        rows = Seq(
                          identificationTypeRow,
                          identificationNumberRow,
                          nationalityRow
                        ).flatten
                      )

                      futureSection
                  }.toSeq

                  Future.sequence(futureSections)
              }
              .getOrElse(Future.successful(Seq.empty))
        }
        .getOrElse(Future.successful(Seq.empty))

    val result: Future[Seq[Section]] =
      Future
        .sequence(Seq(sectionHC, sectionDepartureTransportMeans))
        .map(_.flatten)

    result
  }

}
