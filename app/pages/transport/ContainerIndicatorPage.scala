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
import pages.sections.containers.ContainerSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object ContainerIndicatorPage extends QuestionPage[Boolean] {

  override def path: JsPath = ContainerSection.path \ toString

  override def toString: String = "containerIndicator"

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(routes.ContainerIndicatorController.onPageLoad(departureId, mode))

  // TODO: add clean up when subsequent page is built

}
