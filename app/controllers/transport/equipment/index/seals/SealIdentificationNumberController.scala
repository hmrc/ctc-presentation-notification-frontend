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
import forms.transport.equipment.SealIdentificationNumberFormProvider
import models.requests.{DataRequest, MandatoryDataRequest}
import models.{Index, Mode, RichOptionalJsArray}
import navigation.EquipmentNavigator
import pages.sections.transport.equipment.SealsSection
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transport.equipment.index.seals.SealIdentificationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SealIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  formProvider: SealIdentificationNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  navigator: EquipmentNavigator,
  view: SealIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(SealIdentificationNumberPage(equipmentIndex, sealIndex)) match {
        case None        => form(equipmentIndex, sealIndex)
        case Some(value) => form(equipmentIndex, sealIndex).fill(value)
      }
      Ok(view(preparedForm, departureId, mode, equipmentIndex, sealIndex))
  }

  def onSubmit(departureId: String, mode: Mode, equipmentIndex: Index, sealIndex: Index): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        form(equipmentIndex, sealIndex)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, mode, equipmentIndex, sealIndex))),
            value => redirect(mode, value, departureId, equipmentIndex, sealIndex)
          )

    }

  private def form(equipmentIndex: Index, sealIndex: Index)(implicit request: DataRequest[_]) =
    formProvider("transport.equipment.index.seals.sealIdentificationNumber", otherSealIdentificationNumbers(equipmentIndex, sealIndex))

  private def otherSealIdentificationNumbers(equipmentIndex: Index, sealIndex: Index)(implicit request: DataRequest[_]): Seq[String] = {
    val numberOfSeals = request.userAnswers.get(SealsSection(equipmentIndex)).length
    (0 until numberOfSeals)
      .filterNot(_ == sealIndex.position)
      .map(
        sealInd => SealIdentificationNumberPage(equipmentIndex, Index(sealInd))
      )
      .flatMap(request.userAnswers.get(_))
  }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String,
    equipmentIndex: Index,
    sealIndex: Index
  )(implicit request: MandatoryDataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SealIdentificationNumberPage(equipmentIndex, sealIndex), value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(SealIdentificationNumberPage(equipmentIndex, sealIndex), updatedAnswers, departureId, mode))
}
