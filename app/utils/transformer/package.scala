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

package utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

package object transformer {

  implicit class TryOps[A](tryValue: Try[A]) {
    def asFuture: Future[A] = Future.fromTry(tryValue)
  }

  implicit def liftToFuture[A](f: A => Future[A])(implicit ec: ExecutionContext): Future[A] => Future[A] = _ flatMap f
}
