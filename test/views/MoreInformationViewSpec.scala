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
import views.html.MoreInformationView

class MoreInformationViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[MoreInformationView].apply(lrn.value, departureId)(fakeRequest, messages)

  override val prefix: String = "moreInformation"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    s"When you pre-lodged this declaration, you left out some key information about the movement. Customs now need this information before they can release the goods for transit."
  )
  behave like pageWithContent("p", "Answer the following questions to complete the declaration.")

  behave like pageWithLinkAsButton("Continue", "")
}
