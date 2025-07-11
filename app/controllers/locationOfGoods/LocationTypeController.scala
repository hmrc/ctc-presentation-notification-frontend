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
import models.reference.LocationType
import models.requests.MandatoryDataRequest
import models.{Mode, RichCC015CType}
import navigation.LocationOfGoodsNavigator
import pages.QuestionPage
import pages.locationOfGoods.{InferredLocationTypePage, LocationTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.LocationTypeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.LocationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  locationTypeService: LocationTypeService,
  val controllerComponents: MessagesControllerComponents,
  view: LocationTypeView,
  navigator: LocationOfGoodsNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val isSimplified      = request.userAnswers.departureData.isSimplified
        val ie170LocationType = request.userAnswers.get(LocationTypePage)

        locationTypeService.getLocationTypes(isSimplified).flatMap {
          case locationType :: Nil =>
            redirect(mode, InferredLocationTypePage, locationType, departureId)
          case refDataLocationTypes =>
            val preparedForm = ie170LocationType match {
              case None               => form(refDataLocationTypes)
              case Some(locationType) => form(refDataLocationTypes).fill(locationType)
            }
            Future.successful(Ok(view(preparedForm, departureId, refDataLocationTypes, mode)))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val isSimplified = request.userAnswers.departureData.isSimplified

        locationTypeService.getLocationTypes(isSimplified).flatMap {
          locationTypes =>
            form(locationTypes)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, departureId, locationTypes, mode))),
                value => redirect(mode, LocationTypePage, value, departureId)
              )

        }
    }

  private def form(locationType: Seq[LocationType]): Form[LocationType] =
    formProvider("locationOfGoods.locationType", locationType)

  private def redirect(
    mode: Mode,
    page: QuestionPage[LocationType],
    value: LocationType,
    departureId: String
  )(implicit request: MandatoryDataRequest[?]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))
}
