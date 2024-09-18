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

package controllers

import controllers.actions._
import models.AuditType.PresentationNotification
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.submission.{AuditService, MetricsService, SubmissionService}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  viewModelProvider: PresentationNotificationAnswersViewModelProvider,
  view: CheckYourAnswersView,
  submissionService: SubmissionService,
  auditService: AuditService,
  metricsService: MetricsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(departureId: String): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      viewModelProvider(request.userAnswers, departureId)
        .map {
          viewModel =>
            Ok(view(request.userAnswers.lrn, departureId, viewModel.sections))
        }
  }

  def onSubmit(departureId: String): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      submissionService.submit(request.userAnswers, departureId).map {
        response =>
          val auditType = PresentationNotification
          metricsService.increment(auditType.name, response)
          response.status match {
            case x if is2xx(x) =>
              auditService.audit(auditType, request.userAnswers)
              Redirect(routes.InformationSubmittedController.onPageLoad(departureId))
            case x =>
              logger.error(s"Error submitting IE170: $x")
              Redirect(routes.ErrorController.technicalDifficulties())
          }
      }
  }
}
