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

package models

import models.messages.MessageData
import pages.QuestionPage
import play.api.libs.json._
import queries.Gettable
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: String,
  eoriNumber: EoriNumber,
  lrn: String,
  data: JsObject,
  lastUpdated: Instant,
  departureData: MessageData
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def getOrElse[A](page: QuestionPage[A], findValueInDepartureData: MessageData => Option[A])(implicit reads: Reads[A]): Option[A] =
    get(page) orElse findValueInDepartureData(departureData)

  def set[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A], reads: Reads[A]): Try[UserAnswers] = {
    lazy val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    lazy val cleanup: JsObject => Try[UserAnswers] = d => {
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }

    get(page) match {
      case Some(`value`) => Success(this)
      case _             => updatedData flatMap cleanup
    }
  }

  def remove[A](page: QuestionPage[A]): Try[UserAnswers] = {
    val updatedData    = data.removeObject(page.path).getOrElse(data)
    val updatedAnswers = copy(data = updatedData)
    page.cleanup(None, updatedAnswers)
  }

  def remove[A](page: QuestionPage[A], departureDataPath: JsPath): Try[UserAnswers] = {
    val updated170Data = data.removeObject(page.path).getOrElse(data)

    val Ie15Data: JsObject                = Json.toJson(departureData).as[JsObject]
    val updatedIe15DataJsObject: JsObject = Ie15Data.removeObject(departureDataPath).getOrElse(Ie15Data)

    val updatedDepartureData = Json.fromJson[MessageData](updatedIe15DataJsObject) match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => throw new RuntimeException(s"Failed to convert JsObject to MessageData: $errors")
    }

    val updatedAnswers = copy(data = updated170Data, departureData = updatedDepartureData)
    page.cleanup(None, updatedAnswers)
  }
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit def reads(implicit sensitiveFormats: SensitiveFormats): Reads[UserAnswers] = (
    (__ \ "_id").read[String] and
      (__ \ "eoriNumber").read[EoriNumber] and
      (__ \ "lrn").read[String] and
      (__ \ "data").read[JsObject](sensitiveFormats.jsObjectReads) and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantReads) and
      (__ \ "departureData").read[MessageData](sensitiveFormats.messageDataReads)
  )(UserAnswers.apply _)

  implicit def writes(implicit sensitiveFormats: SensitiveFormats): OWrites[UserAnswers] = (
    (__ \ "_id").write[String] and
      (__ \ "eoriNumber").write[EoriNumber] and
      (__ \ "lrn").write[String] and
      (__ \ "data").write[JsObject](sensitiveFormats.jsObjectWrites) and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantWrites) and
      (__ \ "departureData").write[MessageData](sensitiveFormats.messageDataWrites)
  )(unlift(UserAnswers.unapply))

  implicit def format(implicit sensitiveFormats: SensitiveFormats): Format[UserAnswers] =
    Format(reads, writes)
}
