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

package views.transport.equipment.index.seals

import generators.Generators
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transport.equipment.index.seals.RemoveSealYesNoView

class RemoveSealYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val sealIdNumber = nonEmptyString.sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RemoveSealYesNoView].apply(form, departureId, NormalMode, equipmentIndex, sealIndex, sealIdNumber)(fakeRequest, messages)

  override val prefix: String = "transport.equipment.index.seals.transport.equipment.removeSeal"

  behave like pageWithTitle(sealIdNumber)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport equipment")

  behave like pageWithHeading(sealIdNumber)

  behave like pageWithRadioItems(args = Seq(sealIdNumber))

  behave like pageWithSubmitButton("Continue")
}
