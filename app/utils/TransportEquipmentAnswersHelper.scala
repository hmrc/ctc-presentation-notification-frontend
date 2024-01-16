/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.{EquipmentsSection, SealsSection}
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.index.{AddContainerIdentificationNumberYesNoPage, AddSealYesNoPage, ContainerIdentificationNumberPage}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.{Link, Section}

import scala.concurrent.{ExecutionContext, Future}

class TransportEquipmentAnswersHelper (
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode,
  equipmentIndex: Index
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  private val lastIndex = Index(
    userAnswers
      .get(EquipmentsSection)
      .map(_.value.length - 1)
      .getOrElse(userAnswers.departureData.Consignment.ActiveBorderTransportMeans.map(_.length - 1).getOrElse(0))
  )

  def addAnyTransportEquipmentYesNo(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddTransportEquipmentYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.equipment.addTransportEquipment",
    findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup
    id = Some("change-add-transport-equipment")
  )

  def addContainerIdentificationNumberYesNo(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddContainerIdentificationNumberYesNoPage(equipmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.equipment.index.addContainerIdentificationNumberYesNo",
    findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup
    id = Some("change-add-transport-equipment-container-identification-number-yes-no")
  )

  def containerIdentificationNumber(): Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(equipmentIndex),
    formatAnswer = formatAsText,
    prefix = "transport.equipment.index.containerIdentificationNumber",
    findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup,
    id = Some("change-transport-equipment-container-identification-number")
  )

  def sealsYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddSealYesNoPage(equipmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.equipment.index.addSealYesNo",
    findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup,
    id = Some("change-add-seals")
  )

  def seal(index: Index): Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = SealIdentificationNumberPage(equipmentIndex, index),
      formatAnswer = formatAsText,
      prefix = "transport.equipment.index.checkYourAnswers.seal",
      findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup
      id = Some(s"change-seal-${index.display}"),
      args = index.display
    )

  def seals: Seq[SummaryListRow] = getAnswersAndBuildSectionRows(SealsSection(equipmentIndex))(seal)

  def addOrRemoveSeals: Option[Link] = buildLink(SealsSection(equipmentIndex), false) {
    Link(
      id = "add-or-remove-seals",
      text = messages("transport.equipment.index.checkYourAnswers.seals.addOrRemove"),
      href = controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex).url
    )
  }

  def getSection: Future[Section] = {

    val sealYesNoPage = sealsYesNo
    val addContaoner  = addContainerIdentificationNumberYesNo()
    val addTransport  = addAnyTransportEquipmentYesNo()
    val ua            = userAnswers
    println(ua, addContaoner, addTransport)

    val rows = Seq(
//      addAnyTransportEquipmentYesNo(),
      addContainerIdentificationNumberYesNo(),
      containerIdentificationNumber(),
      sealsYesNo,
      seals
    ).flatten

    val ans = Section(
      sectionTitle = messages("checkYourAnswers.transport.equipment.active.withIndex", equipmentIndex.display),
      rows = rows,
      addAnotherLink = addOrRemoveSeals
    )
    Future.successful(ans)
  }

}

object TransportEquipmentAnswersHelper {

  def apply(userAnswers: UserAnswers,
            departureId: String,
            checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
            mode: Mode,
            activeIndex: Index
  )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier) =
    new TransportEquipmentAnswersHelper(userAnswers, departureId, checkYourAnswersReferenceDataService, mode, activeIndex)
}
