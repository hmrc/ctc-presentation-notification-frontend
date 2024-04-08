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

import config.Constants.Mail
import config.FrontendAppConfig
import models.{CheckMode, Index, UserAnswers}
import pages.sections.transport.border.BorderActiveListSection
import pages.sections.transport.departureTransportMeans.TransportMeansListSection
import pages.transport.InlandModePage
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

      val helper                      = new PresentationNotificationAnswersHelper(userAnswers, departureId, mode)
      val placeOfLoadingAnswersHelper = new PlaceOfLoadingAnswersHelper(userAnswers, departureId, mode)
      val locationOfGoodsHelper       = new LocationOfGoodsAnswersHelper(userAnswers, departureId, mode)
      val inlandModeAnswersHelper     = new InlandModeAnswersHelper(userAnswers, departureId, mode)
      val transitHolderAnswerHelper   = new TransitHolderAnswerHelper(userAnswers, departureId, cyaRefDataService, mode)
      val activeBorderHelper          = new ActiveBorderTransportMeansAnswersHelper(userAnswers, departureId, mode, Index(0))

      val representativeHelper = new RepresentativeAnswersHelper(userAnswers, departureId, mode)

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
                  case (_, i) => Future.successful(activeBorderAnswersViewModelProvider.apply(userAnswers, departureId, mode, Index(i)).section)
                }
                .toSeq
            )
        }
      }

      val departureTransportMeansSections: Seq[Section] = {
        userAnswers.get(InlandModePage) match {
          case Some(value) if value.code == Mail => Seq.empty
          case _ =>
            userAnswers
              .get(TransportMeansListSection)
              .map(_.value.zipWithIndex.map {
                case (_, i) =>
                  new DepartureTransportMeansAnswersHelper(userAnswers, departureId, mode, Index(i)).buildDepartureTransportMeansSection
              }.toSeq)
              .getOrElse(Seq.empty)
        }
      }

      val firstSection = Section(
        rows = Seq(
          helper.customsOfficeDeparture,
          helper.limitDate,
          helper.containerIndicator
        ).flatten
      )

      val representativeSection: Section = representativeHelper.representativeSection
      val locationOfGoods                = locationOfGoodsHelper.locationOfGoodsSection
      val inlandModeSection              = inlandModeAnswersHelper.buildInlandModeSection
      val borderSection                  = helper.borderModeSection

      for {
        transitHolderSection              <- transitHolderAnswerHelper.transitHolderSection
        placeOfLoading                    <- placeOfLoadingAnswersHelper.placeOfLoadingSection
        activeBorderTransportMeansSection <- activeBorderTransportMeansSectionFuture
        transportEquipmentSection = TransportEquipmentAnswersHelper.sections(userAnswers, departureId, mode)
        sections =
          firstSection.toSeq ++ transitHolderSection.toSeq ++ representativeSection.toSeq ++ locationOfGoods.toSeq ++ placeOfLoading.toSeq ++ inlandModeSection.toSeq ++ departureTransportMeansSections ++ borderSection.toSeq ++ activeBorderTransportMeansSection ++ transportEquipmentSection
      } yield new PresentationNotificationAnswersViewModel(sections)

    }

    // scalastyle:on method.length
  }
}
