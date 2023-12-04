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

package viewModels.transport.equipment

import config.FrontendAppConfig
import controllers.transport.equipment.index.{routes => indexRoutes}
import controllers.transport.equipment.routes
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.{AddTransportEquipmentYesNoPage, ItemPage}
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherEquipmentViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call,
  isNumberItemsZero: Boolean
) extends AddAnotherViewModel {
  override val prefix: String                                         = "transport.equipment.addAnotherEquipment"
  override def maxCount(implicit config: FrontendAppConfig): Int      = config.maxEquipmentNumbers
  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < maxCount && !isNumberItemsZero
  def noMoreItemsLabel(implicit messages: Messages): String           = messages(s"$prefix.noMoreItems.label")

}

object AddAnotherEquipmentViewModel {

  class AddAnotherEquipmentViewModelProvider {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, isNumberItemsZero: Boolean)(implicit
      messages: Messages
    ): AddAnotherEquipmentViewModel = {

      val listItems = userAnswers
        .get(EquipmentsSection)
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val equipmentIndex = Index(i)

            def equipmentPrefix(increment: Int)      = messages("transport.prefix", increment)
            def container(id: String)                = messages("transport.container", id)
            val noContainer                          = messages("transport.value.withoutContainer")
            val lessThan2TransportEquipment: Boolean = userAnswers.get(EquipmentsSection).map(_.value.length).getOrElse(0) < 2

            val name = userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex)) flatMap {
              identificationNumber =>
                Some(s"${equipmentPrefix(equipmentIndex.display)} - ${container(identificationNumber)}")
            } orElse {
              userAnswers
                .get(ItemPage(equipmentIndex, Index(0)))
                .map(
                  _ => s"${equipmentPrefix(equipmentIndex.display)} - $noContainer"
                )
            }

            val changeRoute =
              userAnswers.get(ContainerIndicatorPage) match {
                case Some(true) =>
                  controllers.transport.equipment.index.routes.ContainerIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex).url

                case _ if userAnswers.departureData.isSimplified && userAnswers.departureData.hasAuthC523 =>
                  controllers.transport.equipment.index.seals.routes.SealIdentificationNumberController
                    .onPageLoad(departureId, mode, equipmentIndex, Index(0))
                    .url
                case _ => controllers.transport.equipment.index.routes.AddSealYesNoController.onPageLoad(departureId, mode, equipmentIndex).url
              }
            val removeRoute: Option[String] = if (lessThan2TransportEquipment && userAnswers.get(AddTransportEquipmentYesNoPage).isEmpty) {
              None
            } else {
              Some(indexRoutes.RemoveTransportEquipmentController.onPageLoad(departureId, mode, equipmentIndex).url)
            }

            name.map(ListItem(_, changeRoute, removeRoute))
        }
        .toSeq

      new AddAnotherEquipmentViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherEquipmentController.onSubmit(departureId, mode),
        isNumberItemsZero
      )
    }
  }
}
