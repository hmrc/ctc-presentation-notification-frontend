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

package controllers.transport.equipment.index

import controllers.actions._
import forms.YesNoFormProvider
import models.{Index, Mode}
import pages.sections.transport.equipment.EquipmentSection
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.index.RemoveTransportEquipmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTransportEquipmentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTransportEquipmentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(departureId: String, mode: Mode): Call =
    controllers.transport.equipment.routes.AddAnotherEquipmentController.onPageLoad(departureId, mode)

  private def form(equipmentIndex: Index): Form[Boolean] =
    formProvider("transport.equipment.index.removeTransportEquipment", equipmentIndex.display)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, EquipmentSection(equipmentIndex), addAnother(departureId, mode)) {
      implicit request =>
        val identificationNumber: Option[String] = request.userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex))
        Ok(view(form(equipmentIndex), departureId, mode, equipmentIndex, identificationNumber))
    }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, EquipmentSection(equipmentIndex), addAnother(departureId, mode))
    .async {
      implicit request =>
        val identificationNumber: Option[String] = request.userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex))
        form(equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex, identificationNumber))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(EquipmentSection(equipmentIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(departureId, mode))
          )
    }
}
