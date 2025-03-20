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

import forms.SelectableFormProvider.CountryFormProvider
import forms.behaviours.InputSelectViewBehaviours
import models.reference.Nationality
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.transport.departureTransportMeans.TransportMeansNationalityView

class TransportMeansNationalityViewSpec extends InputSelectViewBehaviours[Nationality] {

  private val formProvider = new CountryFormProvider()

  override val field: String = formProvider.field

  override def form: Form[Nationality] = formProvider.apply(prefix, SelectableList(values))

  override def applyView(form: Form[Nationality]): HtmlFormat.Appendable =
    injector.instanceOf[TransportMeansNationalityView].apply(form, departureId, values, NormalMode, transportIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[Nationality] = arbitraryNationality

  override val prefix: String = "consignment.departureTransportMeans.nationality"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Departure means of transport")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("Enter the country or code, like Austria or AT.")

  behave like pageWithSubmitButton("Continue")
}
