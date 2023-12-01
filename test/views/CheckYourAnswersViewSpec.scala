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
import viewModels.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends CheckYourAnswersViewBehaviours {

  override def view: HtmlFormat.Appendable = viewWithSections(sections)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector.instanceOf[CheckYourAnswersView].apply(lrn.value, departureId, sections)(fakeRequest, messages)

  override val prefix: String = "checkYourAnswers"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    s"The information below combines parts of your initial declaration with the answers you just provided. Check this and confirm whether the details are still correct."
  )

  behave like pageWithCheckYourAnswers()

  behave like pageWithContent("h2", "Send this information for your departure declaration")

  behave like pageWithContent("p", "By sending this, you are confirming that these details are correct to the best of your knowledge.")

  behave like pageWithSubmitButton("Confirm and send")
}
