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

import controllers.transport.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.transport.border.BorderSection
import pages.sections.transport.departureTransportMeans.TransportMeansListSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object AddInlandModeOfTransportYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = BorderSection.path \ toString

  override def toString: String = "addInlandModeOfTransport"

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(routes.AddInlandModeOfTransportYesNoController.onPageLoad(departureId, mode))

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(InlandModePage).flatMap(_.remove(TransportMeansListSection))
      case _           => super.cleanup(value, userAnswers)
    }
}
