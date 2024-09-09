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

package controllers.transport.equipment.index.seals

import controllers.actions._
import forms.YesNoFormProvider
import models.requests.SpecificDataRequestProvider1
import models.{Index, Mode}
import pages.sections.transport.equipment.SealSection
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.index.seals.RemoveSealYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveSealYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSealYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[String]#SpecificDataRequest[?]

  private def form(implicit request: Request): Form[Boolean] =
    formProvider("transport.equipment.index.seals.transport.equipment.removeSeal", request.arg)

  private def addAnother(departureId: String, mode: Mode, equipmentIndex: Index): Call =
    controllers.transport.equipment.index.routes.AddAnotherSealController.onPageLoad(departureId, mode, equipmentIndex)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, SealSection(equipmentIndex, sealIndex), addAnother(departureId, mode, equipmentIndex))
    .andThen(getMandatoryPage.getFirst(SealIdentificationNumberPage(equipmentIndex, sealIndex))) {
      implicit request =>
        Ok(view(form, departureId, mode, equipmentIndex, sealIndex, request.arg))
    }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, SealSection(equipmentIndex, sealIndex), addAnother(departureId, mode, equipmentIndex))
    .andThen(getMandatoryPage.getFirst(SealIdentificationNumberPage(equipmentIndex, sealIndex)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex, sealIndex, request.arg))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(SealSection(equipmentIndex, sealIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(departureId, mode, equipmentIndex))
          )
    }
}
