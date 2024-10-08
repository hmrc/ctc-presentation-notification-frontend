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
import controllers.transport.equipment.index.seals.routes
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.SealsSection
import pages.transport.equipment.index.AddSealYesNoPage
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem, RichListItems}

case class AddAnotherSealViewModel(listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "transport.equipment.index.addAnotherSeal"

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
            userAnswers.get(SealIdentificationNumberPage(equipmentIndex, sealIndex)).map {
              name =>
                ListItem(
                  name = name,
                  changeUrl = routes.SealIdentificationNumberController.onPageLoad(departureId, mode, equipmentIndex, sealIndex).url,
                  removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(departureId, mode, equipmentIndex, sealIndex).url)
                )
            }
        }
        .toSeq
        .checkRemoveLinks(userAnswers.get(AddSealYesNoPage(equipmentIndex)).isEmpty)

      new AddAnotherSealViewModel(
        listItems,
        onSubmitCall = controllers.transport.equipment.index.routes.AddAnotherSealController.onSubmit(departureId, mode, equipmentIndex)
      )
    }
  }
}
