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

package views.transport.border.active

import models.NormalMode
import models.reference.transport.border.active.Identification
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transport.border.active.RemoveBorderTransportYesNoView

class RemoveBorderTransportYesNoViewSpec extends YesNoViewBehaviours {

  val identificationType: Identification = Identification("code", "desc")
  val identificationNumber: String       = "1234"

  val insetText: Option[String] = Option(s"$identificationType - $identificationNumber")

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RemoveBorderTransportYesNoView].apply(form, departureId, NormalMode, index, insetText)(fakeRequest, messages)

  override val prefix: String = "transport.border.active.removeBorderTransport"

  behave like pageWithTitle(index.display)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Border means of transport")

  behave like pageWithHeading(index.display)

  behave like pageWithRadioItems(args = Seq(index.display))

  behave like pageWithInsetText(s"${identificationType.asString} - $identificationNumber")

  behave like pageWithSubmitButton("Continue")
}
