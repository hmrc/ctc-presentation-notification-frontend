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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CheckInformationView

class CheckInformationViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[CheckInformationView].apply(lrn.value, departureId)(fakeRequest, messages)

  override val prefix: String = "checkInformation"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    s"Before Customs can release the goods for transit, you need to confirm whether there have been any changes to this declaration since you pre-lodged it."
  )
  behave like pageWithContent("p", "Check your answers and confirm if these details are still correct.")

  behave like pageWithLinkAsButton("Continue", "")
}
