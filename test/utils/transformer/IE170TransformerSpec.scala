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
import generated.CC015CType
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IE170TransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  private val transformer                          = app.injector.instanceOf[IE170Transformer]
  private lazy val mockConsignmentTransformer      = mock[ConsignmentTransformer]
  private lazy val mockTransitOperationTransformer = mock[TransitOperationTransformer]
  private lazy val mockRepresentativeTransformer   = mock[RepresentativeTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsignmentTransformer].toInstance(mockConsignmentTransformer),
        bind[TransitOperationTransformer].toInstance(mockTransitOperationTransformer),
        bind[RepresentativeTransformer].toInstance(mockRepresentativeTransformer)
      )

  private case object FakeConsignmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "consignment"
  }

  private case object FakeTransitOperationSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transitOperation"
  }

  private case object FakeRepresentativeSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "representative"
  }

  "must transform data" in {
    forAll(arbitrary[CC015CType]) {
      ie015 =>

        when(mockConsignmentTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeConsignmentSection, Json.obj("foo" -> "bar")))
          }

        when(mockTransitOperationTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeTransitOperationSection, Json.obj("foo" -> "bar")))
          }

        when(mockRepresentativeTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeRepresentativeSection, Json.obj("foo" -> "bar")))
          }

        val userAnswers = emptyUserAnswers.copy(departureData = ie015)

        val result = transformer.transform(userAnswers).futureValue

        result.getValue(FakeConsignmentSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(FakeTransitOperationSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(FakeRepresentativeSection) mustEqual Json.obj("foo" -> "bar")
    }
  }
}
