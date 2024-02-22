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

import controllers.actions.Actions
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsOfficesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.InformationSubmittedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class InformationSubmittedController @Inject() (
  cc: MessagesControllerComponents,
  actions: Actions,
  view: InformationSubmittedView,
  customsOfficesService: CustomsOfficesService
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        customsOfficesService.getCustomsOfficeById(request.userAnswers.departureData.CustomsOfficeOfDestination).map {
          customsOffice => Ok(view(request.userAnswers.lrn, departureId, customsOffice))
        }
    }
}
