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

package services

import generated.{Flag, Number0, Number1}
import models.Index
import pages.QuestionPage
import play.api.libs.json.{JsArray, JsPath, JsSuccess, Reads}
import scalaxb.XMLCalendar

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.xml.datatype.XMLGregorianCalendar

package object submission {

  def readInferred[T](page: QuestionPage[T], inferredPage: QuestionPage[T])(implicit reads: Reads[T]): Reads[T] =
    page.path.read[T] orElse inferredPage.path.read[T]

  implicit class RichJsPath(value: JsPath) {

    def readArray[T](implicit reads: Index => Reads[T]): Reads[Seq[T]] =
      value
        .readWithDefault(JsArray())
        .map {
          _.value.zipWithIndex
            .flatMap {
              case (jsValue, index) => jsValue.validate[T](reads(Index(index))).asOpt
            }
            .toSeq
        }

    def readNullableSafe[T](implicit reads: Reads[T]): Reads[Option[T]] =
      value.readNullable[T] orElse None
  }

  implicit def boolToFlag(x: Boolean): Flag =
    if (x) Number1 else Number0

  implicit def localDateToXMLGregorianCalendar(date: Option[LocalDate]): Option[XMLGregorianCalendar] =
    date.map(localDateToXMLGregorianCalendar)

  implicit def localDateToXMLGregorianCalendar(date: LocalDate): XMLGregorianCalendar =
    stringToXMLGregorianCalendar(date.toString)

  implicit def stringToXMLGregorianCalendar(date: Option[String]): Option[XMLGregorianCalendar] =
    date.map(stringToXMLGregorianCalendar)

  implicit def stringToXMLGregorianCalendar(date: String): XMLGregorianCalendar =
    XMLCalendar(date.replace("Z", ""))

  implicit def localDateTimeToXMLGregorianCalendar(localDateTime: LocalDateTime): XMLGregorianCalendar = {
    val formatterNoMillis: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    localDateTime.format(formatterNoMillis)
  }

  implicit def successfulReads[T](value: T): Reads[T] = Reads {
    _ => JsSuccess(value)
  }
}
