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
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transport.equipment.AddTransportEquipmentYesNoView

class AddTransportEquipmentYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddTransportEquipmentYesNoView].apply(form, departureId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transport.equipment.addTransportEquipment"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption(messages("transport.caption"))

  behave like pageWithHeading()

  behave like pageWithContent("p", messages("transport.equipment.addTransportEquipment.paragraph"))

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton(messages("site.continue"))
}
