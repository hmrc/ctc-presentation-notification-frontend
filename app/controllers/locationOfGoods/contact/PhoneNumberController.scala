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

package controllers.locationOfGoods.contact

import controllers.actions._
import forms.TelephoneNumberFormProvider
import models.Mode
import models.requests.MandatoryDataRequest
import navigation.LocationOfGoodsNavigator
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.contact.PhoneNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PhoneNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  formProvider: TelephoneNumberFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PhoneNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .andThen(getMandatoryPage(NamePage)) {
      implicit request =>
        val contactName = request.arg
        val form        = formProvider("locationOfGoods.contactPhoneNumber", contactName)
        val preparedForm = request.userAnswers.get(PhoneNumberPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, departureId, contactName, mode))
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .andThen(getMandatoryPage(NamePage))
    .async {
      implicit request =>
        val contactName = request.arg
        val form        = formProvider("locationOfGoods.contactPhoneNumber", contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, contactName, mode))),
            value => redirect(mode, value, departureId)
          )
    }

  private def redirect(
    mode: Mode,
    value: String,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(PhoneNumberPage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(PhoneNumberPage, updatedAnswers, departureId, mode))

}
