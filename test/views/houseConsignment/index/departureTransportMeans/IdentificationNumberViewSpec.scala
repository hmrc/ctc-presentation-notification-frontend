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

package views.houseConsignment.index.departureTransportMeans

import forms.DepartureTransportMeansIdentificationNumberFormProvider
import models.NormalMode
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.houseConsignment.index.departureTransportMeans.IdentificationNumberView

class IdentificationNumberViewSpec extends InputTextViewBehaviours[String] {

  override val prefix: String = "houseConsignment.index.departureTransportMeans.identificationNumber"

  private val identificationType = arbitrary[TransportMeansIdentification].sample.value

  override def form: Form[String] = new DepartureTransportMeansIdentificationNumberFormProvider()(prefix, houseConsignmentIndex.display)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[IdentificationNumberView]
      .apply(form, departureId, NormalMode, houseConsignmentIndex, houseConsignmentDepartureTransportMeansIndex, identificationType.asString)(fakeRequest,
                                                                                                                                              messages
      )

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(houseConsignmentIndex.display)

  behave like pageWithBackLink()

  behave like pageWithHeading(houseConsignmentIndex.display)

  behave like pageWithSectionCaption("Departure means of transport")

  behave like pageWithHint("This can be up to 35 characters long and include both letters and numbers.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Continue")
}
