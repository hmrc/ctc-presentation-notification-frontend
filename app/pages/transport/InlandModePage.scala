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

package pages.transport

import models.reference.TransportMode.InlandMode
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.transport.TransportSection
import pages.sections.transport.departureTransportMeans.TransportMeansSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object InlandModePage extends QuestionPage[InlandMode] {

  override def path: JsPath = TransportSection.path \ toString

  override def toString: String = "inlandModeOfTransport"

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(controllers.transport.routes.InlandModeController.onPageLoad(departureId, mode))

  override def cleanup(value: Option[InlandMode], userAnswers: UserAnswers): Try[UserAnswers] = {
    val transportMeansPath: JsPath = JsPath \ "Consignment" \ "DepartureTransportMeans"

    value match {
      case Some(value) if value.code == "5" => userAnswers.remove(TransportMeansSection, transportMeansPath)
      case _                                => super.cleanup(value, userAnswers)
    }
  }
}
