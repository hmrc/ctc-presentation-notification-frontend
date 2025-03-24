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

package views.transport.border.active

import forms.SelectableFormProvider.CustomsOfficeFormProvider
import forms.behaviours.InputSelectViewBehaviours
import models.reference.CustomsOffice
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.transport.border.active.CustomsOfficeActiveBorderView

class CustomsOfficeActiveBorderViewSpec extends InputSelectViewBehaviours[CustomsOffice] {

  private val formProvider = new CustomsOfficeFormProvider()

  override val field: String = formProvider.field

  override def form: Form[CustomsOffice] = formProvider.apply(prefix, SelectableList(values))

  override def applyView(form: Form[CustomsOffice]): HtmlFormat.Appendable =
    injector.instanceOf[CustomsOfficeActiveBorderView].apply(form, departureId, values, NormalMode, activeIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[CustomsOffice] = arbitraryCustomsOffice

  override val prefix: String = "transport.border.active.customsOfficeActiveBorder"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Border means of transport")

  behave like pageWithHeading()

  behave like pageWithContent("p", "You can only select a location from the offices of transit, exit or destination in your transit route.")

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")
}
