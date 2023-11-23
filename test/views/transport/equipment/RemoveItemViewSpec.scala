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

package views.transport.equipment

import models.NormalMode
import models.reference.Item
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transport.equipment.RemoveItemView

class RemoveItemViewSpec extends YesNoViewBehaviours {

  private val equipmentIdNumber = equipmentIndex.display
  private val item              = Item(1234, "desc")

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RemoveItemView].apply(form, departureId, NormalMode, equipmentIndex, itemIndex, item)(fakeRequest, messages)

  override val prefix: String = "transport.equipment.removeItem"

  behave like pageWithTitle(equipmentIdNumber)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport equipment")

  behave like pageWithHeading(equipmentIdNumber)

  behave like pageWithInsetText(s"Item ${itemIndex.display} - ${item.description}")

  behave like pageWithRadioItems(args = Seq(equipmentIdNumber))

  behave like pageWithSubmitButton("Save and continue")
}
