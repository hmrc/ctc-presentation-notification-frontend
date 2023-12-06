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

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transport.equipment.AddAnotherEquipmentViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.transport.equipment.AddAnotherEquipmentView

class AddAnotherEquipmentViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxEquipmentNumbers

  private def formProvider(viewModel: AddAnotherEquipmentViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherEquipmentViewModel].sample.value
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)
  private val noMoreItemsViewModel = viewModel.copy(listItems = Nil, isNumberItemsZero = true)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherEquipmentView]
      .apply(form, departureId, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherEquipmentView]
      .apply(formProvider(maxedOutViewModel), departureId, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  def applyNoMoreItemsView: HtmlFormat.Appendable = injector
    .instanceOf[AddAnotherEquipmentView]
    .apply(formProvider(noMoreItemsViewModel), departureId, noMoreItemsViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "transport.equipment.addAnotherEquipment"

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport equipment")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count)()

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count)

  behave like pageWithSubmitButton("Save and continue")

  "page with no more items" - {

    val doc = parseView(applyNoMoreItemsView)
    behave like pageWithContent(
      doc,
      "p",
      "You cannot add any more transport equipment as there are no more items to apply to it. To add another transport equipment, you need to add an item first."
    )
  }

}
