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

package base

import models.reference.transport.border.active.Identification
import models.{EoriNumber, RichJsObject, UserAnswers}
import play.api.libs.json.{JsObject, JsPath, Json}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Main {

  def main(args: Array[String]): Unit = {
    println(JsPath \ "name")
    println(JsPath \ 0)
    println(JsPath \ 1)
    val path  = JsPath \ "transport" \ "transportMeansActiveList" \ 2 \ "identification"
    var data  = Json.parse("""{"transport":{"addAnotherBorderMeansOfTransport":true}}""").as[JsObject]
    val value = Identification("code0", "description")
    println(data)
    println(path)
    println(path.path)
    (data = data.setObject(JsPath \ "transport" \ "transportMeansActiveList" \ 0 \ "identification", Json.toJson(value)).asOpt.get)
    (data = data.setObject(JsPath \ "transport" \ "transportMeansActiveList" \ 1 \ "identification", Json.toJson(value.copy(code = "code1"))).asOpt.get)
    (data = data.setObject(JsPath \ "transport" \ "transportMeansActiveList" \ 2 \ "identification", Json.toJson(value.copy(code = "code2"))).asOpt.get)
    (data = data.setObject(JsPath \ "transport" \ "transportMeansActiveList" \ 3 \ "identification", Json.toJson(value.copy(code = "code3"))).asOpt.get)
    (data = data.setObject(JsPath \ "transport" \ "transportMeansActiveList" \ 4 \ "identification", Json.toJson(value.copy(code = "code4"))).asOpt.get)
    println(">>>>>>>>>>>>>>>>>>>>>>")
    println(data)
    println(">>>>>>>>>>>>>>>>>>>>>>")
    println(data.removeObject(JsPath \ "transport" \ "transportMeansActiveList" \ 2 \ "identification"))
    data = data.removeObject(JsPath \ "transport" \ "transportMeansActiveList" \ 2 \ "identification").asOpt.get

    println(">>>>>>>>>>>>>>>>>>>>>>")
    println(data.removeObject(JsPath \ "transport" \ "transportMeansActiveList" \ 2 \ "identification"))

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    def function1(userAnswers: UserAnswers): Future[UserAnswers] =
      // Your implementation here
      Future.successful(userAnswers)

    def function2(userAnswers: UserAnswers): Try[UserAnswers] =
      // Your implementation here
      Try(userAnswers)

    def function3(userAnswers: UserAnswers): Future[UserAnswers] =
      // Your implementation here
      Future.successful(userAnswers)

    // Implicit lifting function
    implicit def liftToFuture[A](a: Try[A]): Future[A] = Future.fromTry(a)

    // Usage with for-yield
    val result: Future[UserAnswers] = for {
      result1 <- function1(UserAnswers("", EoriNumber(""), "", JsObject.empty, Instant.EPOCH, TestMessageData.messageData))
      result2 <- function2(result1)
//      result3 <- function3(result2)
    } yield result2

    result.onComplete {
      case scala.util.Success(value)     => println(s"Result: $value")
      case scala.util.Failure(exception) => println(s"Error: $exception")
    }

    Thread.sleep(1000)
  }
}
