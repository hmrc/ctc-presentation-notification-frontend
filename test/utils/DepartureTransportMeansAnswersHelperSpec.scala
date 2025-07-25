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

package utils

import base.SpecBase
import generators.Generators
import models.Mode
import models.reference.Nationality
import models.reference.transport.transportMeans.TransportMeansIdentification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transport.InlandModePage
import pages.transport.departureTransportMeans._

class DepartureTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DepartureTransportMeansAnswersHelper" - {

    "identification number row" - {
      "must return None when no identification number in ie13/ie15/170" - {
        s"when $TransportMeansIdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, transportIndex)
              val result = helper.identificationNumberRow
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        s"when $TransportMeansIdentificationNumberPage defined in the ie170" in {
          forAll(arbitrary[Mode], Gen.alphaNumStr) {
            (mode, number) =>
              val answers = emptyUserAnswers
                .setValue(TransportMeansIdentificationNumberPage(transportIndex), number)
              val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, mode, transportIndex)

              val result = helper.identificationNumberRow.get

              result.key.value mustEqual "Identification"
              result.value.value mustEqual number
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationNumberController
                .onPageLoad(departureId, mode, transportIndex)
                .url
              action.visuallyHiddenText.get mustEqual "the identification for the departure means of transport"
              action.id mustEqual "change-departure-transport-means-identification-number"
          }
        }
      }
    }
  }

  "identification type row" - {
    "must return None when no identification type in 170" - {
      s"when $TransportMeansIdentificationPage undefined" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, transportIndex)
            val result = helper.identificationType
            result must not be defined
        }
      }
    }

    "must return Some(Row)" - {
      s"when $TransportMeansIdentificationPage defined in the ie170" in {
        forAll(arbitrary[Mode], arbitrary[TransportMeansIdentification]) {
          (mode, means) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansIdentificationPage(transportIndex), means)

            val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, mode, transportIndex)

            val result = helper.identificationType.get

            result.key.value mustEqual "Identification type"
            result.value.value mustEqual means.toString
            val actions = result.actions.get.items
            actions.size mustEqual 1
            val action = actions.head
            action.content.value mustEqual "Change"
            action.href mustEqual controllers.transport.departureTransportMeans.routes.TransportMeansIdentificationController
              .onPageLoad(departureId, mode, transportIndex)
              .url
            action.visuallyHiddenText.get mustEqual "identification type for the departure means of transport"
            action.id mustEqual "change-transport-means-identification"
        }
      }
    }
  }

  "nationality type row" - {
    "must return None when no nationality in 170" - {
      s"when $TransportMeansNationalityPage undefined" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, departureId, mode, transportIndex)
            val result = helper.nationality
            result must not be defined
        }
      }
    }

    "must return Some(Row)" - {
      s"when $TransportMeansNationalityPage defined in the ie170" in {
        forAll(arbitrary[Mode], arbitrary[Nationality]) {
          (mode, nationality) =>
            val answers = emptyUserAnswers
              .setValue(TransportMeansNationalityPage(transportIndex), nationality)

            val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, mode, transportIndex)
            val result = helper.nationality.get

            result.key.value mustEqual "Registered country"
            result.value.value mustEqual nationality.description
            val actions = result.actions.get.items
            actions.size mustEqual 1
            val action = actions.head
            action.content.value mustEqual "Change"
            action.href mustEqual controllers.transport.departureTransportMeans.routes.TransportMeansNationalityController
              .onPageLoad(departureId, mode, transportIndex)
              .url
            action.visuallyHiddenText.get mustEqual "registered country for the departure means of transport"
            action.id mustEqual "change-departure-transport-means-nationality"
        }
      }
    }
  }

  "buildDepartureTransportMeansSection" - {

    "must return Some(Section()) when inland mode is not 5 " in {
      forAll(arbitrary[Mode], arbitraryInlandModeOfTransport.arbitrary.suchThat(_.isNotOneOf("5"))) {
        (mode, inlandMode) =>
          val answers = emptyUserAnswers
            .setValue(InlandModePage, inlandMode)
            .setValue(TransportMeansIdentificationPage(transportIndex), TransportMeansIdentification("type", "desc"))
            .setValue(TransportMeansIdentificationNumberPage(transportIndex), "12345")
            .setValue(TransportMeansNationalityPage(transportIndex), Nationality("code", "desc"))

          val helper =
            new DepartureTransportMeansAnswersHelper(answers, departureId, mode, transportIndex)
          val result = helper.buildDepartureTransportMeansSection

          result.rows.size mustEqual 3
          result.sectionTitle.get mustEqual "Departure means of transport 1"
      }
    }

    "must return Some(Section()) when inland mode is not present" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val answers = emptyUserAnswers
            .setValue(TransportMeansIdentificationPage(transportIndex), TransportMeansIdentification("type", "desc"))
            .setValue(TransportMeansIdentificationNumberPage(transportIndex), "12345")
            .setValue(TransportMeansNationalityPage(transportIndex), Nationality("code", "desc"))

          val helper = new DepartureTransportMeansAnswersHelper(answers, departureId, mode, transportIndex)
          val result = helper.buildDepartureTransportMeansSection

          result.rows.size mustEqual 3
          result.sectionTitle.get mustEqual "Departure means of transport 1"
      }
    }

  }

}
