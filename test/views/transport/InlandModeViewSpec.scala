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

package views.transport

import forms.EnumerableFormProvider
import models.NormalMode
import models.reference.TransportMode.InlandMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.EnumerableViewBehaviours
import views.html.transport.InlandModeView

class InlandModeViewSpec extends EnumerableViewBehaviours[InlandMode] {

  override def form: Form[InlandMode] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[InlandMode]): HtmlFormat.Appendable =
    injector.instanceOf[InlandModeView].apply(form, departureId, values, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transport.inlandModeOfTransport"

  override def radioItems(fieldId: String, checkedValue: Option[InlandMode] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[InlandMode] = Seq(
    InlandMode("1", "Maritime"),
    InlandMode("2", "Rail")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Transport details - Inland mode of transport")

  behave like pageWithHeading("Which inland mode of transport are you using?")

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Continue")
}
