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

import cats.data.ReaderT
import models.OpsError.WriterError
import models.UserAnswers
import models.requests.MandatoryDataRequest
import pages.QuestionPage
import play.api.libs.json._
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpVerbs.GET

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

package object controllers {

  type EitherType[A]        = Either[WriterError, A]
  type UserAnswersWriter[A] = ReaderT[EitherType, UserAnswers, A]
  type Write[A]             = (QuestionPage[A], UserAnswers)

  implicit class SettableOps[A](page: QuestionPage[A]) {

    def writeToUserAnswers(value: A)(implicit format: Format[A]): UserAnswersWriter[Write[A]] =
      ReaderT[EitherType, UserAnswers, Write[A]](
        userAnswers =>
          userAnswers.set[A](page, value) match {
            case Success(userAnswers) => Right((page, userAnswers))
            case Failure(exception)   => Left(WriterError(page, Some(s"Failed to write $value to page ${page.path} with exception: ${exception.toString}")))
          }
      )

    def removeFromUserAnswers(): UserAnswersWriter[Write[A]] =
      ReaderT[EitherType, UserAnswers, Write[A]] {
        userAnswers =>
          userAnswers.remove(page) match {
            case Success(value)     => Right((page, value))
            case Failure(exception) => Left(WriterError(page, Some(s"Failed to remove ${page.path} with exception: ${exception.toString}")))
          }
      }
  }

  implicit class SettableOpsRunner[A](userAnswersWriter: UserAnswersWriter[Write[A]]) {

    def appendValue[B](subPage: QuestionPage[B], value: B)(implicit format: Format[B]): UserAnswersWriter[Write[A]] =
      userAnswersWriter.flatMapF {
        case (page, userAnswers) =>
          userAnswers.set(subPage, value) match {
            case Success(value)     => Right((page, value))
            case Failure(exception) => Left(WriterError(page, Some(s"Failed to append value to answer: ${exception.getMessage}")))
          }
      }

    def writeToSession(
      userAnswers: UserAnswers
    )(implicit sessionRepository: SessionRepository, executionContext: ExecutionContext): Future[Write[A]] =
      userAnswersWriter.run(userAnswers) match {
        case Left(opsError) => Future.failed(new Exception(s"${opsError.toString}"))
        case Right(value) =>
          sessionRepository
            .set(value._2)
            .flatMap {
              case true  => Future.successful(value)
              case false => Future.failed(new Exception(s"Lock already in place"))
            }
      }

    def writeToSession()(implicit
      dataRequest: MandatoryDataRequest[?],
      sessionRepository: SessionRepository,
      ex: ExecutionContext
    ): Future[Write[A]] = writeToSession(dataRequest.userAnswers)
  }

  implicit class NavigatorOps[A](write: Future[Write[A]]) {

    def navigateTo(call: Call)(implicit executionContext: ExecutionContext): Future[Result] =
      navigate {
        _ => call
      }

    def navigateTo(route: String)(implicit executionContext: ExecutionContext): Future[Result] =
      navigateTo(Call(GET, route))

    private def navigate(result: Write[A] => Call)(implicit executionContext: ExecutionContext): Future[Result] =
      write.map {
        w => Redirect(result(w))
      }
  }
}
