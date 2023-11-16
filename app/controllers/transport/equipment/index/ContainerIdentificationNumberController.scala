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

import controllers.SettableOps
import controllers.actions._
import forms.transport.equipment.ContainerIdentificationNumberFormProvider
import models.requests.{DataRequest, MandatoryDataRequest}
import models.{Index, LocalReferenceNumber, Mode, RichOptionalJsArray}
import navigation.Navigator
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.equipment.index.ContainerIdentificationNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.index.ContainerIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContainerIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  formProvider: ContainerIdentificationNumberFormProvider,
  navigator: Navigator,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: ContainerIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(equipmentIndex: Index)(implicit request: DataRequest[_]): Form[String] =
    formProvider("equipment.index.containerIdentificationNumber", otherContainerIdentificationNumbers(equipmentIndex))

  private def otherContainerIdentificationNumbers(equipmentIndex: Index)(implicit request: DataRequest[_]): Seq[String] = {
    val numberOfEquipments = request.userAnswers.get(EquipmentsSection).length
    (0 until numberOfEquipments)
      .map(Index(_))
      .filterNot(_ == equipmentIndex)
      .map(ContainerIdentificationNumberPage)
      .flatMap(request.userAnswers.get(_))
  }

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex)) match {
        case None        => form(equipmentIndex)
        case Some(value) => form(equipmentIndex).fill(value)
      }
      Ok(view(preparedForm, departureId, mode, equipmentIndex))
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      form(equipmentIndex)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex))),
          value => redirect(mode, value, departureId, equipmentIndex)
        )

  }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String,
    equipmentIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(ContainerIdentificationNumberPage(equipmentIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(ContainerIdentificationNumberPage(equipmentIndex), updatedAnswers, departureId, mode))
}
