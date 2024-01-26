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

package controllers.transport

import config.FrontendAppConfig
import controllers.actions._
import forms.transport.DateFormProvider
import models.Mode
import navigation.{LocationOfGoodsNavigator, Navigator}
import pages.transport.LimitDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{CustomsOfficesService, DateTimeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RichLocalDate
import views.html.transport.LimitDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LimitDateController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigator: LocationOfGoodsNavigator,
  formProvider: DateFormProvider,
  actions: Actions,
  config: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: LimitDateView,
  dateTimeService: DateTimeService,
  customsOfficesService: CustomsOfficesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private lazy val maxDate    = dateTimeService.plusMinusDays(config.limitDateDaysAfter)
  private lazy val maxDateArg = maxDate.plusDays(1).formatForText

  private def form: Form[LocalDate] = {
    val minDate = dateTimeService.plusMinusDays(config.limitDateDaysBefore)
    formProvider("transport.limit.date", minDate, maxDate)
  }

  def onPageLoad(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val customsOfficeOfDestinationId = request.userAnswers.departureData.CustomsOfficeOfDestination

        customsOfficesService.getCustomsOfficeById(customsOfficeOfDestinationId).map {
          customsOffice =>
            val customsOfficeString: String = customsOffice.map(_.toString).getOrElse(customsOfficeOfDestinationId)

            val preparedForm = request.userAnswers.get(LimitDatePage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, mode, departureId, maxDateArg, customsOfficeString))
        }
    }

  def onSubmit(departureId: String, mode: Mode): Action[AnyContent] = actions
    .requireData(departureId)
    .async {
      implicit request =>
        val customsOfficeOfDestinationId = request.userAnswers.departureData.CustomsOfficeOfDestination

        customsOfficesService.getCustomsOfficeById(customsOfficeOfDestinationId).flatMap {
          customsOffice =>
            val customsOfficeString: String = customsOffice.map(_.toString).getOrElse(customsOfficeOfDestinationId)

            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, departureId, maxDateArg, customsOfficeString))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(LimitDatePage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(LimitDatePage, updatedAnswers, departureId, mode))
              )
        }
    }
}
