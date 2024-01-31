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

package views.transport.departureTransportMeans

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transport.departureTransportMeans.AddAnotherTransportMeansViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.transport.departureTransportMeans.AddAnotherTransportMeansView

class AddAnotherTransportMeansViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxTransportMeans

  private def formProvider(viewModel: AddAnotherTransportMeansViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherTransportMeansViewModel].sample.value
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherTransportMeansView]
      .apply(form, departureId, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherTransportMeansView]
      .apply(formProvider(maxedOutViewModel), departureId, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "consignment.departureTransportMeans.addAnotherTransportMeans"

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Departure means of transport")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count)()

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count)

  behave like pageWithContent(
    "p",
    "You must only add one departure means of transport if you are using maritime, air, fixed transport installations or inland waterways for your inland mode. Your declaration will be rejected if you add more than one."
  )

  behave like pageWithContent(
    "p",
    "If you are using road as your mode, you can add up to 3 departure means of transport. For rail, you can add up to 999 departure means."
  )

  behave like pageWithSubmitButton("Continue")
}
