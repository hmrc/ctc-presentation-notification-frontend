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

package base

import config.FrontendAppConfig
import models.{EoriNumber, LocalReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with AppWithDefaultMockFixtures {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val eoriNumber: EoriNumber     = EoriNumber("eoriNumber")

  def emptyUserAnswers: UserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now())

  val departureId: String = "AB123"

  val lrn: LocalReferenceNumber = LocalReferenceNumber("ABCD1234567890123")

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)
}
