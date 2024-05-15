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

package controllers.transport.departureTransportMeans

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.transport.transportMeans.TransportMeansIdentification
import models.{NormalMode, TransportMeans, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import pages.transport.departureTransportMeans.{TransportMeansIdentificationNumberPage, TransportMeansIdentificationPage}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transport.departureTransportMeans.RemoveDepartureTransportMeansYesNoView

class RemoveDepartureTransportMeansYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("consignment.departureTransportMeans.removeDepartureTransportMeans")
  private val mode         = NormalMode

  private lazy val removeDepartureTransportMeansRoute =
    routes.RemoveDepartureTransportMeansYesNoController.onPageLoad(departureId, mode, transportIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveBorderTransportYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[TransportMeansIdentification], nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(transportIndex), identifier)
            .setValue(TransportMeansIdentificationNumberPage(transportIndex), identificationNumber)

          val insetText = TransportMeans(identifier, Some(identificationNumber)).asString

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveDepartureTransportMeansYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, departureId, mode, transportIndex, insetText)(request, messages).toString
      }
    }

    "must return OK and the correct view for a GET with no identificationNumber" in {

      forAll(arbitrary[TransportMeansIdentification]) {
        identifier =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(transportIndex), identifier)

          val insetText = TransportMeans(identifier, None).asString

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveDepartureTransportMeansYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, departureId, mode, transportIndex, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another departureTransportMeans and remove departureTransportMeans at specified index" in {
        forAll(arbitrary[TransportMeansIdentification]) {
          identifier =>
            beforeEach()

            val userAnswers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(transportIndex), identifier)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.AddAnotherTransportMeansController.onPageLoad(departureId, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.get(TransportMeansSection(transportIndex)) mustNot be(defined)
        }
      }
    }

    "when no submitted" - {
      "must redirect to add another departureTransportMeans and not remove departureTransportMeans at specified index" in {
        forAll(arbitrary[TransportMeansIdentification]) {
          identifier =>
            beforeEach()

            val userAnswers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(transportIndex), identifier)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.AddAnotherTransportMeansController.onPageLoad(departureId, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.get(TransportMeansSection(transportIndex)) must be(defined)
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[TransportMeansIdentification], nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(transportIndex), identifier)
            .setValue(TransportMeansIdentificationNumberPage(transportIndex), identificationNumber)

          val insetText = TransportMeans(identifier, Option(identificationNumber)).asString

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeDepartureTransportMeansRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveDepartureTransportMeansYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, departureId, mode, transportIndex, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
