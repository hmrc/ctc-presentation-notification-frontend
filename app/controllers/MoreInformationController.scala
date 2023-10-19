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
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MoreInformationView

import javax.inject.{Inject, Singleton}

@Singleton
class MoreInformationController @Inject() (
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: MoreInformationView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val lrn = request.userAnswers.lrn
      Ok(view(lrn, departureId))
  }

  def onSubmit(departureId: String): Action[AnyContent] = actions.requireData(departureId) {
    implicit request =>
      val locationOfGoods = request.userAnswers.departureData.Consignment.LocationOfGoods
      val isSimplified    = request.userAnswers.departureData.isSimplified

      val nextPage = locationOfGoods match {
        case None if !isSimplified => controllers.locationOfGoods.routes.LocationTypeController.onPageLoad(departureId, mode = NormalMode)
        case None                  => controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(departureId, mode = NormalMode)
      }

      Redirect(nextPage)
  }
}
