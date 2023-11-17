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
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.SealsSection
import pages.transport.equipment.{AddSealYesNoPage, SealIdentificationNumberPage}
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherSealViewModel(listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "transport.equipment.addAnotherSeal"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSeals
}

object AddAnotherSealViewModel {

  class AddAnotherSealViewModelProvider() {

    def apply(userAnswers: UserAnswers, departureId: String, mode: Mode, equipmentIndex: Index): AddAnotherSealViewModel = {

      val listItems = userAnswers
        .get(SealsSection(equipmentIndex))
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val sealIndex = Index(i)

            val name = userAnswers.get(SealIdentificationNumberPage(equipmentIndex, sealIndex))

            val changeRoute = "#" // TODO Add change route when CYA page is done

            val removeRoute: Option[String] = if (userAnswers.get(AddSealYesNoPage(equipmentIndex)).isEmpty && sealIndex.isFirst) {
              None
            } else {
              Some(Call("GET", "#").url) // TODO Update remove route when remove controller is added
            }

            name.map(ListItem(_, changeRoute, removeRoute))
        }
        .toSeq

      new AddAnotherSealViewModel(
        listItems,
        onSubmitCall = controllers.transport.equipment.routes.AddAnotherSealController.onSubmit(departureId, mode, equipmentIndex)
      )
    }
  }
}
