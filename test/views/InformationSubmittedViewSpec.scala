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

import generators.Generators
import models.reference.CustomsOffice
import play.twirl.api.HtmlFormat
import views.behaviours.PanelViewBehaviours
import views.html.InformationSubmittedView

class InformationSubmittedViewSpec extends PanelViewBehaviours with Generators {

  val office: CustomsOffice = arbitraryCustomsOffice.arbitrary.sample.get

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[InformationSubmittedView].apply(lrn.value, office)(fakeRequest, messages)

  override val prefix: String = "information.sent"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithPanel(
    body = s"for Local Reference Number (LRN) $lrn"
  )

  behave like pageWithContent("h2", "What happens next")

  behave like pageWithLink(
    "new-departure",
    "Make another departure declaration",
    frontendAppConfig.manageTransitMovementsDeclareNewDeclarationUrl
  )

  behave like pageWithContent("h2", "Before you go")

  behave like pageWithLink(
    "feedback",
    "Take a short survey",
    frontendAppConfig.feedbackUrl
  )
}
