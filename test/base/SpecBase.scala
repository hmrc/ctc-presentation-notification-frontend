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

import base.TestMessageData.messageData
import config.FrontendAppConfig
import models.{EoriNumber, Index, LocalReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues, TryValues}
import pages.QuestionPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.{Format, Json, Reads}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with EitherValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with AppWithDefaultMockFixtures {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val eoriNumber: EoriNumber     = EoriNumber("eoriNumber")

  def emptyUserAnswers: UserAnswers = UserAnswers(departureId, eoriNumber, lrn.value, Json.obj(), Instant.now(), messageData)

  val departureId: String = "651431d7e3b05b21"

  val lrn: LocalReferenceNumber = LocalReferenceNumber("ABCD1234567890123")

  val index: Index                                        = Index(0)
  val activeIndex: Index                                  = Index(0)
  val equipmentIndex: Index                               = Index(0)
  val itemIndex: Index                                    = Index(0)
  val sealIndex: Index                                    = Index(0)
  val houseConsignmentIndex: Index                        = Index(0)
  val houseConsignmentDepartureTransportMeansIndex: Index = Index(0)

  def injector: Injector = app.injector

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit class RichUserAnswers(userAnswers: UserAnswers) {

    def getValue[T](page: QuestionPage[T])(implicit rds: Reads[T]): T =
      userAnswers.get(page).value

    def setValue[T](page: QuestionPage[T], value: T)(implicit format: Format[T]): UserAnswers =
      userAnswers.set(page, value).success.value

    def setValue[T](page: QuestionPage[T], value: Option[T])(implicit format: Format[T]): UserAnswers =
      value.map(setValue(page, _)).getOrElse(userAnswers)

    def setValue[T](page: QuestionPage[T], f: UserAnswers => T)(implicit format: Format[T]): UserAnswers =
      setValue(page, f(userAnswers))

    def removeValue(page: QuestionPage[_]): UserAnswers =
      userAnswers.remove(page).success.value
  }
}
