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

import forms.SelectableFormProvider
import forms.behaviours.InputSelectViewBehaviours
import models.reference.Item
import models.{Index, NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transport.equipment.SelectItemsViewModel
import views.html.transport.equipment.SelectItemsView

class SelectItemsViewSpec extends InputSelectViewBehaviours[Item] {

  override def form: Form[Item] = new SelectableFormProvider()(prefix, SelectableList(values))

  private val viewModel = SelectItemsViewModel(emptyUserAnswers)

  override def applyView(form: Form[Item]): HtmlFormat.Appendable =
    injector.instanceOf[SelectItemsView].apply(form, Index(0), Index(0), departureId, viewModel.copy(SelectableList(values)), NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[Item] = arbitraryItem

  override val prefix: String = "transport.equipment.selectItems"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport equipment")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")
}
