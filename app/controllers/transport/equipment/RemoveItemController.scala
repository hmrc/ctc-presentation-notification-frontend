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

package controllers.transport.equipment

import controllers.actions._
import forms.YesNoFormProvider
import models.{Index, Mode}
import pages.sections.transport.equipment.ItemSection
import pages.transport.equipment.ItemPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.RemoveItemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveItemController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: RemoveItemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(departureId: String, mode: Mode, equipmentIndex: Index): Call =
    controllers.transport.equipment.routes.ApplyAnotherItemController.onPageLoad(departureId, mode, equipmentIndex)

  private def form(equipmentIndex: Index): Form[Boolean] =
    formProvider("transport.equipment.removeItem", equipmentIndex.display)

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, ItemSection(equipmentIndex, itemIndex), addAnother(departureId, mode, equipmentIndex))
    .andThen(getMandatoryPage(ItemPage(equipmentIndex, itemIndex))) {
      implicit request =>
        Ok(view(form(equipmentIndex), departureId, mode, equipmentIndex, itemIndex, request.arg))
    }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index, itemIndex: Index): Action[AnyContent] = actions
    .requireIndex(departureId, ItemSection(equipmentIndex, itemIndex), addAnother(departureId, mode, equipmentIndex))
    .andThen(getMandatoryPage(ItemPage(equipmentIndex, itemIndex)))
    .async {
      implicit request =>
        form(equipmentIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex, itemIndex, request.arg))),
            value =>
              for {
                updatedAnswers <-
                  if (value) {
                    Future.fromTry(request.userAnswers.remove(ItemSection(equipmentIndex, itemIndex)))
                  } else {
                    Future.successful(request.userAnswers)
                  }
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(addAnother(departureId, mode, equipmentIndex))
          )
    }
}
