/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.{Index, Mode, UserAnswers}
import pages.sections.transport.equipment.EquipmentsSection
import pages.transport.equipment.AddTransportEquipmentYesNoPage
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class TransportEquipmentAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
  mode: Mode,
  activeIndex: Index
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  private val lastIndex = Index(
    userAnswers
      .get(EquipmentsSection)
      .map(_.value.length - 1)
      .getOrElse(userAnswers.departureData.Consignment.ActiveBorderTransportMeans.map(_.length - 1).getOrElse(0))
  )

  def addAnyTransportEquipmentYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddTransportEquipmentYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transport.equipment.addTransportEquipment",
    findValueInDepartureData = message => Option(message.Consignment.TransportEquipment.isDefined),
    id = Some("change-add-transport-equipment")
  )

  def getSection(): Future[Section] = {

    val rows = Seq(
      addAnyTransportEquipmentYesNo
    ).flatten

    val ans = Section(
      sectionTitle = messages("checkYourAnswers.transport.equipment.active.withIndex", activeIndex.display),
      rows = rows,
      addAnotherLink = (userAnswers.departureData.CustomsOfficeOfTransitDeclared, lastIndex == activeIndex) match {
        case (Some(_), true) => None //TODO addOrRemoveActiveBorderTransportsMeans()
        case _               => None
      }
    )
    Future.successful(ans)
  }

}

object TransportEquipmentAnswersHelper {

  def apply(userAnswers: UserAnswers,
            departureId: String,
            checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService,
            mode: Mode,
            activeIndex: Index
  )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier) =
    new TransportEquipmentAnswersHelper(userAnswers, departureId, checkYourAnswersReferenceDataService, mode, activeIndex)
}
