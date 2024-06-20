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

import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{NormalMode, TransportMeans}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transport.departureTransportMeans.RemoveDepartureTransportMeansYesNoView

class RemoveDepartureTransportMeansYesNoViewSpec extends YesNoViewBehaviours {

  val identificationType: TransportMeansIdentification = TransportMeansIdentification("80", "European vessel identification number (ENI Code)")

  val identificationNumber: Option[String] = Some("1234")

  val insetText: String = TransportMeans(identificationType, identificationNumber).asString

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveDepartureTransportMeansYesNoView]
      .apply(form, departureId, NormalMode, transportIndex, insetText)(fakeRequest, messages)

  override val prefix: String = "consignment.departureTransportMeans.removeDepartureTransportMeans"

  behave like pageWithTitle(index.display)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Departure means of transport")

  behave like pageWithHeading(index.display)

  behave like pageWithRadioItems(args = Seq(index.display))

  behave like pageWithInsetText(s"${identificationType.asString} - ${identificationNumber.get}")

  behave like pageWithSubmitButton("Continue")
}
