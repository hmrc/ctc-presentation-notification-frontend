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

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait PageTransformer {

  type DomainModelType
  type ExtractedTypeInDepartureData
  type CapturedAnswer = (QuestionPage[DomainModelType], DomainModelType)

  def transformFromDeparture(
    userAnswers: UserAnswers,
    extractDataFromDepartureData: UserAnswers => Seq[ExtractedTypeInDepartureData],
    generateCapturedAnswers: Seq[ExtractedTypeInDepartureData] => Seq[CapturedAnswer]
  )(implicit writes: Writes[DomainModelType], reads: Reads[DomainModelType]): Future[UserAnswers] = Option
    .when(shouldTransform(userAnswers)) {
      val dataFromDepartureData = extractDataFromDepartureData(userAnswers)
      val capturedAnswers       = generateCapturedAnswers(dataFromDepartureData)

      collectAllCapturedAnswers(userAnswers, capturedAnswers).asFuture
    }
    .getOrElse(successful(userAnswers))

  def shouldTransform: UserAnswers => Boolean = _ => true

  private def collectAllCapturedAnswers(userAnswers: UserAnswers, capturedAnswers: Seq[CapturedAnswer])(implicit
    writes: Writes[DomainModelType],
    reads: Reads[DomainModelType]
  ) =
    capturedAnswers
      .foldLeft(Try(userAnswers)) {
        (accTry, capturedAnswer) =>
          accTry.flatMap(_.set(capturedAnswer._1, capturedAnswer._2))
      }

  // TODO - can we refactor to use an ID to get a particular DomainModelType rather than getting the entire list?
  def transformFromDepartureWithRefData(
    userAnswers: UserAnswers,
    fetchReferenceData: () => Future[Seq[DomainModelType]],
    extractDataFromDepartureData: UserAnswers => Seq[ExtractedTypeInDepartureData],
    generateCapturedAnswers: (Seq[ExtractedTypeInDepartureData], Seq[DomainModelType]) => Seq[CapturedAnswer]
  )(implicit ec: ExecutionContext, writes: Writes[DomainModelType], reads: Reads[DomainModelType]): Future[UserAnswers] = Option
    .when(shouldTransform(userAnswers)) {
      fetchReferenceData().flatMap {
        dataFromRefDB: Seq[DomainModelType] =>
          val dataFromDepartureData = extractDataFromDepartureData(userAnswers)
          val capturedAnswers       = generateCapturedAnswers(dataFromDepartureData, dataFromRefDB)
          Future.fromTry(collectAllCapturedAnswers(userAnswers, capturedAnswers))
      }
    }
    .getOrElse(successful(userAnswers))

  def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers]
}
