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

package controllers.transport.border.active

import controllers.actions._
import forms.YesNoFormProvider
import models.removable.TransportMeans
import models.{Index, Mode, UserAnswers}
import pages.sections.transport.border.BorderActiveSection
import pages.transport.border.active.IdentificationPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.border.active.RemoveBorderTransportYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveBorderTransportYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveBorderTransportYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(activeIndex: Index): Form[Boolean] =
    formProvider("transport.border.active.removeBorderTransport", activeIndex.display)

  private def addAnother(departureId: String, mode: Mode): Call =
    controllers.transport.border.active.routes.AddAnotherBorderMeansOfTransportYesNoController.onPageLoad(departureId, mode)

  private def formatInsetText(userAnswers: UserAnswers, transportMeansIndex: Index): Option[String] =
    TransportMeans(userAnswers, transportMeansIndex).flatMap(_.forRemoveDisplay)

  def onPageLoad(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, BorderActiveSection(activeIndex), addAnother(departureId, mode))
    .andThen(getMandatoryPage(IdentificationPage(activeIndex))) {
      implicit request =>
        val insetText: Option[String] = formatInsetText(request.userAnswers, activeIndex)
        Ok(view(form(activeIndex), departureId, mode, activeIndex, insetText))
    }

  def onSubmit(departureId: String, mode: Mode, activeIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, BorderActiveSection(activeIndex), addAnother(departureId, mode))
    .andThen(getMandatoryPage(IdentificationPage(activeIndex)))
    .async {
      implicit request =>
        val insetText: Option[String] = formatInsetText(request.userAnswers, activeIndex)
        form(activeIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, activeIndex, insetText))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(BorderActiveSection(activeIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(departureId, mode))
          )
    }
}
