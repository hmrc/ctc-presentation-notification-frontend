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

import forms.UnLocodeFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputTextViewBehaviours
import views.html.locationOfGoods.UnLocodeView

class UnLocodeViewSpec extends InputTextViewBehaviours[String] {

  override def form: Form[String] = new UnLocodeFormProvider()(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[UnLocodeView].apply(form, departureId, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(nonEmptyString)

  override val prefix: String = "locationOfGoods.unLocode"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithHint("Enter the code, like DEBER or ESMAD.")

  behave like pageWithContent(
    "p",
    "This is a 5-character code used to identify a transit-related location, like a port or clearance depot."
  )

  behave like pageWithSubmitButton("Continue")
}
