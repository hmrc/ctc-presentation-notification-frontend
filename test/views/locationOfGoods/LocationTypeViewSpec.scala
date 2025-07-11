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

package views.locationOfGoods

import config.Constants.TypeOfLocation.*
import forms.EnumerableFormProvider
import models.NormalMode
import models.reference.LocationType
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.EnumerableViewBehaviours
import views.html.locationOfGoods.LocationTypeView

class LocationTypeViewSpec extends EnumerableViewBehaviours[LocationType] {

  override def form: Form[LocationType] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[LocationType]): HtmlFormat.Appendable =
    injector.instanceOf[LocationTypeView].apply(form, departureId, values, NormalMode)(fakeRequest, messages)

  override val prefix: String = "locationOfGoods.locationType"

  override def radioItems(fieldId: String, checkedValue: Option[LocationType] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[LocationType] = Seq(
    LocationType(DesignatedLocation, "Designated location"),
    LocationType(AuthorisedPlace, "Authorised place")
  )
  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Location of goods")

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    "This is where Customs will carry out any physical checks."
  )

  behave like pageWithContent(
    "p",
    "For goods not present at a customs office, this is their location at the time they are declared."
  )

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Continue")
}
