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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContent, Results}

import java.time.Instant
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  def harness(departureId: String)(f: OptionalDataRequest[AnyContent] => Unit): Unit = {

    lazy val actionProvider = app.injector.instanceOf[DataRetrievalActionProviderImpl]

    actionProvider(departureId)
      .invokeBlock(
        IdentifierRequest(fakeRequest, eoriNumber),
        {
          request: OptionalDataRequest[AnyContent] =>
            f(request)
            Future.successful(Results.Ok)
        }
      )
      .futureValue
  }

  "a data retrieval action" - {

    "must return an OptionalDataRequest with an empty UserAnswers" - {

      "where there are no existing answers for this departureId" in {

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

        harness(departureId) {
          _.userAnswers must not be defined
        }
      }
    }

    "must return an OptionalDataRequest with some defined UserAnswers" - {

      "when there are existing answers for this departureId" in {

        when(mockSessionRepository.get(any())) thenReturn Future.successful(
          Some(UserAnswers(departureId, eoriNumber, lrn.value, JsObject.empty, Instant.now()))
        )

        harness(departureId) {
          _.userAnswers mustBe defined
        }
      }
    }
  }
}
