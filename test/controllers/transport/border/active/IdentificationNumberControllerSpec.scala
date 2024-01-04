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

package controllers.transport.border.active

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import forms.border.IdentificationNumberFormProvider
import generators.Generators
import models.messages.ActiveBorderTransportMeans
import models.reference.transport.border.active.Identification
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.border.active.{IdentificationNumberPage, IdentificationPage}
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MeansOfTransportIdentificationTypesActiveService
import views.html.transport.border.active.IdentificationNumberView

import scala.concurrent.Future

class IdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val identificationType1 = Identification("40", "IATA flight number")

  private val prefix = "transport.border.active.identificationNumber"

  private val formProvider = new IdentificationNumberFormProvider()
  private val form         = formProvider(prefix)
  private val mode         = NormalMode

  private val mockMeansOfTransportIdentificationTypesActiveService: MeansOfTransportIdentificationTypesActiveService =
    mock[MeansOfTransportIdentificationTypesActiveService]

  private lazy val identificationNumberRoute =
    controllers.transport.border.active.routes.IdentificationNumberController.onPageLoad(departureId, mode, index).url

  private val validAnswer = "testString"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind(classOf[MeansOfTransportIdentificationTypesActiveService]).toInstance(mockMeansOfTransportIdentificationTypesActiveService))

  "IdentificationNumber Controller" - {

    "must return Ok and the correct view for a get" in {
      forAll(arbitrary[Identification]) {
        identifier =>
          val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(
            None
          )(
            emptyUserAnswers
              .setValue(IdentificationPage(index), identifier)
          )

          setExistingUserAnswers(userAnswers)
          val request = FakeRequest(GET, identificationNumberRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[IdentificationNumberView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, departureId, mode, index, identifier.asString)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered in the 15" in {

      when(mockMeansOfTransportIdentificationTypesActiveService.getBorderMeansIdentification(any())(any()))
        .thenReturn(Future.successful(identificationType1))

      val userAnswers = UserAnswers.setBorderMeansAnswersLens.set(
        Some(
          Seq(
            ActiveBorderTransportMeans(
              sequenceNumber = "99",
              customsOfficeAtBorderReferenceNumber = None,
              typeOfIdentification = Some(identificationType1.code),
              identificationNumber = Some(identificationType1.code),
              nationality = None,
              conveyanceReferenceNumber = None
            )
          )
        )
      )(emptyUserAnswers)

      setExistingUserAnswers(userAnswers)

      val request    = FakeRequest(GET, identificationNumberRoute)
      val filledForm = form.bind(Map("value" -> "40"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, departureId, mode, index, identificationType1.asString)(request, messages).toString

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      forAll(arbitrary[Identification]) {
        identifier =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(index), identifier)
            .setValue(IdentificationNumberPage(index), "testString")

          setExistingUserAnswers(userAnswers)

          val request    = FakeRequest(GET, identificationNumberRoute)
          val filledForm = form.bind(Map("value" -> "testString"))

          val result = route(app, request).value

          val view = injector.instanceOf[IdentificationNumberView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, departureId, mode, index, identifier.asString)(request, messages).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      forAll(arbitrary[Identification]) {
        identifier =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(index), identifier)

          setExistingUserAnswers(userAnswers)

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val request = FakeRequest(POST, identificationNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" - {
      "when identification type page has been answered" in {
        forAll(arbitrary[Identification]) {
          identifier =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(index), identifier)
            setExistingUserAnswers(userAnswers)

            val request    = FakeRequest(POST, identificationNumberRoute).withFormUrlEncodedBody(("value", ""))
            val filledForm = form.bind(Map("value" -> ""))

            val result = route(app, request).value

            status(result) mustEqual BAD_REQUEST

            val view = injector.instanceOf[IdentificationNumberView]

            contentAsString(result) mustEqual
              view(filledForm, departureId, mode, index, identifier.asString)(request, messages).toString
        }
      }

    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
