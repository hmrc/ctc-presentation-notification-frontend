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
import controllers.transport.equipment.routes
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.ItemsSection
import pages.transport.equipment.ItemPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class ApplyAnotherItemViewModel(listItems: Seq[ListItem], onSubmitCall: Call, equipmentIndex: Index) extends AddAnotherViewModel {
  override val prefix: String = "transport.equipment.applyAnotherItem"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxItems

  override def title(implicit messages: Messages): String   = messages(s"$prefix.$singularOrPlural.title", count, equipmentIndex.display.toString)
  override def heading(implicit messages: Messages): String = messages(s"$prefix.$singularOrPlural.heading", count, equipmentIndex.display.toString)
  override def legend(implicit messages: Messages): String  = messages(s"$prefix.label", equipmentIndex.display.toString)
}

object ApplyAnotherItemViewModel {

  class ApplyAnotherItemViewModelProvider() {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): ApplyAnotherItemViewModel = {

      val listItems = userAnswers
        .get(ItemsSection(equipmentIndex))
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val itemIndex = Index(i)

            val name = userAnswers.get(ItemPage(equipmentIndex, itemIndex)).map(_.toString)

            val changeRoute = routes.SelectItemsController.onSubmit(departureId, mode, equipmentIndex, itemIndex).url

            val removeRoute: Option[String] = Some(Call("GET", "#").url) //TODO: To be done as part of CTCP-4056

            name.map(ListItem(_, changeRoute, removeRoute))
        }
        .toSeq

      new ApplyAnotherItemViewModel(
        listItems,
        onSubmitCall = routes.ApplyAnotherItemController.onSubmit(departureId, mode, equipmentIndex),
        equipmentIndex
      )
    }
  }
}