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

package viewModels

import config.FrontendAppConfig
import models.{CheckMode, Index, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Json}
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils._
import viewModels.transport.border.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class PresentationNotificationAnswersViewModel(sections: Seq[Section])

object PresentationNotificationAnswersViewModel {

  class PresentationNotificationAnswersViewModelProvider @Inject() (implicit
    val config: FrontendAppConfig,
    activeBorderAnswersViewModelProvider: ActiveBorderAnswersViewModelProvider,
    cyaRefDataService: CheckYourAnswersReferenceDataService
  ) {

    // scalastyle:off method.length
    def apply(userAnswers: UserAnswers, departureId: String)(implicit
      messages: Messages,
      ec: ExecutionContext,
      hc: HeaderCarrier
    ): Future[PresentationNotificationAnswersViewModel] = {
      val mode = CheckMode

      val helper                      = new PresentationNotificationAnswersHelper(userAnswers, departureId, cyaRefDataService, mode)
      val placeOfLoadingAnswersHelper = new PlaceOfLoadingAnswersHelper(userAnswers, departureId, cyaRefDataService, mode)
      val locationOfGoodsHelper       = new LocationOfGoodsAnswersHelper(userAnswers, departureId, cyaRefDataService, mode)
      val inlandModeAnswersHelper     = new InlandModeAnswersHelper(userAnswers, departureId, cyaRefDataService, mode)
      val transitHolderAnswerHelper   = new TransitHolderAnswerHelper(userAnswers, departureId, cyaRefDataService, mode)
      val activeBorderHelper          = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, cyaRefDataService, mode, Index(0))

      val firstSection = Section(
        rows = Seq(
          helper.limitDate,
          helper.containerIndicator
        ).flatten
      )

      val activeBorderTransportMeansSectionFuture: Future[Seq[Section]] = {
        (userAnswers.get(BorderActiveListSection), userAnswers.departureData.Consignment.ActiveBorderTransportMeans.isDefined) match {
          case (None, false) =>
            Future.successful(
              Section(sectionTitle = messages("checkYourAnswers.transportMeans.active.withoutIndex"),
                      rows = Seq(activeBorderHelper.addBorderMeansOfTransportYesNo).flatten
              ).toSeq
            )
          case _ =>
            Future.sequence(
              userAnswers
                .get(BorderActiveListSection)
                .getOrElse(
                  userAnswers.departureData.Consignment.ActiveBorderTransportMeans match {
                    case Some(departureActiveBorderMeans) => Json.toJson(departureActiveBorderMeans).as[JsArray]
                    case None                             => JsArray()
                  }
                )
                .value
                .zipWithIndex
                .map {
                  case (_, i) => activeBorderAnswersViewModelProvider.apply(userAnswers, departureId, cyaRefDataService, mode, Index(i)).map(_.section)
                }
                .toSeq
            )
        }
      }

      for {
        transitHolderSection              <- transitHolderAnswerHelper.transitHolderSection
        borderSection                     <- helper.borderModeSection
        placeOfLoading                    <- placeOfLoadingAnswersHelper.placeOfLoadingSection
        locationOfGoods                   <- locationOfGoodsHelper.locationOfGoodsSection
        inlandMode                        <- inlandModeAnswersHelper.buildInlandModeSection
        activeBorderTransportMeansSection <- activeBorderTransportMeansSectionFuture
        sections =
          firstSection.toSeq ++ transitHolderSection.toSeq ++ borderSection.toSeq ++ placeOfLoading.toSeq ++ inlandMode.toSeq ++ activeBorderTransportMeansSection ++ locationOfGoods.toSeq
      } yield new PresentationNotificationAnswersViewModel(sections)

    }

    // scalastyle:on method.length
  }
}
