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

import models.reference.Item
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.{EquipmentsSection, ItemsSection, SealsSection}
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.index.{AddContainerIdentificationNumberYesNoPage, AddSealYesNoPage, ContainerIdentificationNumberPage}
import pages.transport.equipment.{AddTransportEquipmentYesNoPage, ItemPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.{Link, Section}

import scala.concurrent.ExecutionContext

class TransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  mode: Mode,
  equipmentIndex: Index
)(implicit messages: Messages, ec: ExecutionContext)
    extends AnswersHelper(userAnswers, departureId, mode) {

  private val lastIndex: Index = Index(
    userAnswers
      .get(EquipmentsSection)
      .map(_.value.length - 2)
      .getOrElse(0)
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

  def addOrRemoveSeals(): Option[Link] = buildLink(SealsSection(equipmentIndex), doesSectionExistInDepartureData = false) {
    Link(
      id = "add-or-remove-seals",
      text = messages("transport.equipment.index.checkYourAnswers.seals.addOrRemove"),
      href = controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex).url
    )
  }

  def addOrRemoveEquipments(): Option[Link] = buildLink(EquipmentsSection, doesSectionExistInDepartureData = false) {
    Link(
      id = "add-or-remove-transport-equipment",
      text = messages("checkYourAnswers.transportEquipment.addOrRemove"),
      href = controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode).url
    )
  }

  def item(index: Index): Option[SummaryListRow] =
    getAnswerAndBuildRow[Item](
      page = ItemPage(equipmentIndex, index),
      formatAnswer = formatAsItem,
      prefix = "transport.equipment.index.checkYourAnswers.item",
      findValueInDepartureData = _ => None, //TODO not needed as this should be read into ie170 data on app startup
      id = Some(s"change-item-${index.display}"),
      args = index.display
    )

  def items: Seq[SummaryListRow] = getAnswersAndBuildSectionRows(ItemsSection(equipmentIndex))(item)

  def addOrRemoveItems(): Option[Link] = buildLink(ItemsSection(equipmentIndex), doesSectionExistInDepartureData = false) {
    Link(
      id = "add-or-remove-items",
      text = messages("transport.equipment.index.checkYourAnswers.items.addOrRemove"),
      href = controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex).url
    )
  }

  private val preSection: Section = Section(
    rows = Seq(addAnyTransportEquipmentYesNo(), addContainerIdentificationNumberYesNo(), containerIdentificationNumber()).flatten
  )

  private def sealAndItemsSections: Seq[Section] = {

    val sectionSeals: Section =
      Section(
        sectionTitle = messages("checkYourAnswers.transport.equipment.active.withIndex", equipmentIndex.display),
        rows = Seq(sealsYesNo, seals).flatten,
        addAnotherLink = addOrRemoveSeals()
      )

    val secondLink: Option[Link] = if (equipmentIndex == lastIndex) addOrRemoveEquipments() else None

    val sectionItems: Section =
      Section(
        rows = items,
        addAnotherLink = addOrRemoveItems(),
        addSecondLink = secondLink,
        optionalInformationHeading = messages("checkYourAnswers.transportEquipment.item.subHeading")
      )

    Seq(sectionSeals, sectionItems)
  }

}

object TransportEquipmentAnswersHelper {

  def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, activeIndex: Index)(implicit messages: Messages, ec: ExecutionContext) =
    new TransportEquipmentAnswersHelper(userAnswers, departureId, mode, activeIndex)

  def sections(userAnswers: UserAnswers, departureId: String, mode: Mode)(implicit messages: Messages, ec: ExecutionContext): Seq[Section] = {

    def transportEquipmentHelper(index: Int) =
      TransportEquipmentAnswersHelper(userAnswers, departureId, mode, Index(index))

    val pre: Section = transportEquipmentHelper(0).preSection

    userAnswers
      .get(EquipmentsSection) match {
      case Some(jsArray) =>
        val transportSec: Seq[Section] = jsArray.value.zipWithIndex.flatMap {
          case (_, i) =>
            transportEquipmentHelper(i).sealAndItemsSections

        }.toSeq
        pre +: transportSec

      case None =>
        Section(
          sectionTitle = messages("checkYourAnswers.transport.equipment.active.withoutIndex"),
          rows = Seq(transportEquipmentHelper(0).addAnyTransportEquipmentYesNo()).flatten
        ).toSeq

    }

  }
}
