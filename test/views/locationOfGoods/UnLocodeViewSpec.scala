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

import forms.behaviours.InputSelectViewBehaviours
import forms.SelectableFormProvider
import models.reference.UnLocode
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.locationOfGoods.UnLocodeView

class UnLocodeViewSpec extends InputSelectViewBehaviours[UnLocode] {

  private val unLocode1                                        = UnLocode("ABC", "val 1")
  private val unLocode2                                        = UnLocode("DEF", "val 2")
  private val unLocodeSelectableList: SelectableList[UnLocode] = SelectableList.apply(Seq(unLocode1, unLocode2))

  override def form: Form[UnLocode] = new SelectableFormProvider()(prefix, unLocodeSelectableList)

  override def applyView(form: Form[UnLocode]): HtmlFormat.Appendable =
    injector.instanceOf[UnLocodeView].apply(form, unLocodeSelectableList.values, departureId, lrn.toString, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[UnLocode] = arbitraryUnLocode

  override val prefix: String = "locationOfGoods.unLocode"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithHint("Enter the code, like DEBER or ESMAD.")

  behave like pageWithSectionCaption("Location of goods")

  behave like pageWithSubmitButton("Save and continue")
}
