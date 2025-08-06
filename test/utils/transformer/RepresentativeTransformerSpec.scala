/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.transformer

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.RepresentativeType06
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ActingAsRepresentativePage, QuestionPage}
import pages.representative.EoriPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class RepresentativeTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: RepresentativeTransformer = app.injector.instanceOf[RepresentativeTransformer]

  private lazy val mockContactPersonTransformer = mock[ContactPersonTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ContactPersonTransformer].toInstance(mockContactPersonTransformer)
      )

  private case object FakeContactPersonSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "contactPerson"
  }

  "must transform data" - {
    "when representative defined" in {
      forAll(
        arbitrary[RepresentativeType06].map(
          _.copy(
            identificationNumber = "EORI"
          )
        )
      ) {
        representative =>
          when(mockContactPersonTransformer.transform(any())(any()))
            .thenReturn {
              ua => Future.successful(ua.setValue(FakeContactPersonSection, Json.obj("foo" -> "bar")))
            }
          val result = transformer.transform(Some(representative))(hc).apply(emptyUserAnswers).futureValue
          result.getValue(EoriPage) mustEqual representative.identificationNumber
          result.getValue(ActingAsRepresentativePage) mustEqual true
          result.getValue(FakeContactPersonSection) mustEqual Json.obj("foo" -> "bar")
      }
    }

    "when representative undefined" in {
      val result = transformer.transform(None)(hc).apply(emptyUserAnswers).futureValue
      result.get(EoriPage) must not be defined
      result.getValue(ActingAsRepresentativePage) mustEqual false
    }
  }
}
