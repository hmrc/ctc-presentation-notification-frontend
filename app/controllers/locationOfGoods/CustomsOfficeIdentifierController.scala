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

package controllers.locationOfGoods

import controllers.actions._
import forms.SelectableFormProvider
import models.{Mode, RichCC015CType}
import navigation.LocationOfGoodsNavigator
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CustomsOfficesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.CustomsOfficeIdentifierView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeIdentifierController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  actions: Actions,
  formProvider: SelectableFormProvider,
  customsOfficesService: CustomsOfficesService,
  val controllerComponents: MessagesControllerComponents,
  view: CustomsOfficeIdentifierView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val ie170CustomsOffice = request.userAnswers.get(CustomsOfficeIdentifierPage)
        customsOfficesService.getCustomsOfficesOfDepartureForCountry(request.userAnswers.departureData.countryOfDeparture).map {
          customsOfficeList =>
            val form = formProvider("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
            val preparedForm = ie170CustomsOffice match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, departureId, customsOfficeList.values, mode))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        customsOfficesService
          .getCustomsOfficesOfDepartureForCountry(request.userAnswers.departureData.countryOfDeparture)
          .flatMap {
            customsOfficeList =>
              val form = formProvider("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
              form
                .bindFromRequest()
                .fold(
                  formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, customsOfficeList.values, mode))),
                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomsOfficeIdentifierPage, value))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(CustomsOfficeIdentifierPage, updatedAnswers, departureId, mode))
                )
          }
    }
}
