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

package controllers.testOnly

import models.{SensitiveFormats, UserAnswers}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, MessagesControllerComponents}
import services.submission.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class TestOnlySubmissionController @Inject() (
  cc: MessagesControllerComponents,
  submissionService: SubmissionService
)(implicit sensitiveFormats: SensitiveFormats)
    extends FrontendController(cc)
    with Logging {

  def submit(): Action[JsValue] = Action(parse.json) {
    request =>
      request.body.validate[UserAnswers](UserAnswers.format) match {
        case JsSuccess(userAnswers, _) =>
          Ok(submissionService.buildXml(userAnswers))
        case JsError(errors) =>
          logger.info(s"Failed to validate request body as UserAnswers: ${errors.mkString}")
          BadRequest
      }
  }

}
