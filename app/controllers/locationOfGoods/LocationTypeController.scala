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
import forms.EnumerableFormProvider
import models.ProcedureType.Normal
import models.{LocationType, Mode}
import pages.locationOfGoods.LocationTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.LocationTypeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.LocationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  locationTypeService: LocationTypeService,
  val controllerComponents: MessagesControllerComponents,
  view: LocationTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(locationType: Seq[LocationType]): Form[LocationType] =
    formProvider("locationOfGoods.locationType", locationType)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        // TODO get from request
        locationTypeService.getLocationTypes(Normal).flatMap {
          // TODO add in inferral
//          case locationType :: Nil =>
//            redirect(mode, InferredLocationTypePage, locationType)
          case locationTypes =>
            val preparedForm = request.userAnswers.get(LocationTypePage) match {
              case None        => form(locationTypes)
              case Some(value) => form(locationTypes).fill(value)
            }
            Future.successful(Ok(view(preparedForm, departureId, request.userAnswers.lrn, locationTypes, mode)))
        }
    }
}
