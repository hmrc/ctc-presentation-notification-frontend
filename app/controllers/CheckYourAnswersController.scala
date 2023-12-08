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
import models.{LocationOfGoodsIdentification, LocationType}
import navigation.Navigator
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{LocationOfGoodsIdentificationTypeService, LocationTypeService, TransportModeCodesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.PresentationNotificationAnswersViewModel.PresentationNotificationAnswersViewModelProvider
import views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            actions: Actions,
                                            val controllerComponents: MessagesControllerComponents,
                                            viewModelProvider: PresentationNotificationAnswersViewModelProvider,
                                            navigator: Navigator,
                                            borderModeService: TransportModeCodesService,
                                            identificationTypeService: LocationOfGoodsIdentificationTypeService,
                                            view: CheckYourAnswersView
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = actions.requireData(departureId).async {
    implicit request =>
      val presentationNotificationAnswersViewModel = for {
        borderModeCodes <- borderModeService.getBorderModes()
        identificationTypes <- request.userAnswers.departureData.Consignment.LocationOfGoods
          .map(
            goods => LocationType(goods.typeOfLocation, "")
          ) match {
          case Some(value) => identificationTypeService.getLocationOfGoodsIdentificationTypes(value)
          case None => Future.successful(Seq[LocationOfGoodsIdentification]())
        }
        sections = viewModelProvider(request.userAnswers, departureId, borderModeCodes, identificationTypes)
      } yield sections

      presentationNotificationAnswersViewModel
        .flatMap {
          _.map {
            viewModel =>
              Ok(view(request.userAnswers.lrn, departureId, viewModel.sections))
          }
        }
  }


  def onSubmit(departureId: String): Action[AnyContent] = actions.requireData(departureId) {
    implicit request => //todo will redirect to Declaration submitted page once implemented
      ???
  }
}
