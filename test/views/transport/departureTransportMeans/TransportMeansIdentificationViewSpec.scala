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

import forms.EnumerableFormProvider
import models.NormalMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.EnumerableViewBehaviours
import views.html.transport.departureTransportMeans.TransportMeansIdentificationView

class TransportMeansIdentificationViewSpec extends EnumerableViewBehaviours[TransportMeansIdentification] {

  override def form: Form[TransportMeansIdentification] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[TransportMeansIdentification]): HtmlFormat.Appendable =
    injector.instanceOf[TransportMeansIdentificationView].apply(form, departureId, values, NormalMode, transportIndex)(fakeRequest, messages)

  override val prefix: String = "consignment.departureTransportMeans.identification"

  override def radioItems(fieldId: String, checkedValue: Option[TransportMeansIdentification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[TransportMeansIdentification] = Seq(
    TransportMeansIdentification("80", "European vessel identification number (ENI Code)"),
    TransportMeansIdentification("81", "Name of an inland waterways vehicle")
  )

  behave like pageWithTitle(index.display)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Departure means of transport")

  behave like pageWithHeading(index.display)

  behave like pageWithContent(
    "p",
    "If you are using road as your inland mode and this is your first departure means of transport, you must add the registration number of the road vehicle. From your second departure means onwards, you must add the registration number of the road trailer."
  )

  behave like pageWithRadioItems(args = Seq(index.display))

  behave like pageWithSubmitButton("Continue")
}
