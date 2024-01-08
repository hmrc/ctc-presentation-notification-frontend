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

package views.representative

import forms.TelephoneNumberFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.TelephoneNumberViewBehaviours
import views.html.representative.RepresentativePhoneNumberView

class RepresentativePhoneNumberViewSpec extends TelephoneNumberViewBehaviours {

  override val prefix: String = "representative.representativeTelephoneNumber"

  override def form: Form[String] = new TelephoneNumberFormProvider()(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[RepresentativePhoneNumberView].apply(form, departureId, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSectionCaption("Representative")

  behave like pageWithHint("Include the country code, for example +44 808 157 0192.")

  behave like pageWithTelephoneNumberInput()

  behave like pageWithSubmitButton("Continue")
}
