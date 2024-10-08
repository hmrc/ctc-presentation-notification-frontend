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

package generators

import generated.CC015CType
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsValue, Json}

import java.time.Instant

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id         <- arbitrary[String]
        eoriNumber <- arbitrary[EoriNumber]
        lrn        <- nonEmptyString
        data       <- Gen.const(Map[QuestionPage[?], JsValue]())
        ie015Data  <- arbitrary[CC015CType]
      } yield UserAnswers(
        id = id,
        eoriNumber = eoriNumber,
        lrn = lrn,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        },
        lastUpdated = Instant.now(),
        departureData = ie015Data
      )
    }
  }
}
