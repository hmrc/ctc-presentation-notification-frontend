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
import models.requests.SpecificDataRequestProvider1
import models.{LocationOfGoodsIdentification, LocationType, Mode}
import navigation.LocationOfGoodsNavigator
import navigation.annotations.LocationOfGoods
import pages._
import pages.locationOfGoods.{IdentificationPage, InferredIdentificationPage, InferredLocationTypePage, LocationTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.LocationOfGoodsIdentificationTypeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  @LocationOfGoods navigator: LocationOfGoodsNavigator,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: EnumerableFormProvider,
  locationOfGoodsIdentificationTypeService: LocationOfGoodsIdentificationTypeService,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[LocationType]#SpecificDataRequest[_]

  private def form(locationOfGoodsIdentification: Seq[LocationOfGoodsIdentification]): Form[LocationOfGoodsIdentification] =
    formProvider("locationOfGoods.identification", locationOfGoodsIdentification)

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .andThen(getMandatoryPage(LocationTypePage, InferredLocationTypePage))
    .async {
      implicit request =>
        locationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(request.arg).flatMap {
          case identifier :: Nil =>
            redirect(mode, InferredIdentificationPage, identifier, departureId)
          case identifiers =>
            val preparedForm = request.userAnswers.get(IdentificationPage) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }

            Future.successful(Ok(view(preparedForm, departureId, request.userAnswers.lrn, identifiers, mode)))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .andThen(getMandatoryPage(LocationTypePage, InferredLocationTypePage))
    .async {
      implicit request =>
        locationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(request.arg).flatMap {
          locationOfGoodsIdentificationTypes =>
            form(locationOfGoodsIdentificationTypes)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, departureId, request.userAnswers.lrn, locationOfGoodsIdentificationTypes, mode))),
                value => redirect(mode, IdentificationPage, value, departureId)
              )
        }
    }

  private def redirect(
    mode: Mode,
    page: QuestionPage[LocationOfGoodsIdentification],
    value: LocationOfGoodsIdentification,
    departureId: String
  )(implicit request: Request): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(page, updatedAnswers, departureId, mode))
}
