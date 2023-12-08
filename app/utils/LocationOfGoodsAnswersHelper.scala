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

package utils

import config.FrontendAppConfig
import models.{LocationOfGoodsIdentification, LocationType, Mode, UserAnswers}
import pages.locationOfGoods.{AuthorisationNumberPage, IdentificationPage, LocationTypePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

class LocationOfGoodsAnswersHelper(
  userAnswers: UserAnswers,
  departureId: String,
  locationTypes: Seq[LocationType],
  identificationTypes: Seq[LocationOfGoodsIdentification],
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def locationType: Option[SummaryListRow] = getAnswerAndBuildRow[LocationType](
    page = LocationTypePage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.locationType",
    findValueInDepartureData = message =>
      message.Consignment.LocationOfGoods.flatMap(
        value =>
          locationTypes.find {
            lt => value.typeOfLocation == lt.code
          }
      ),
    id = Some("change-location-type")
  )

  def qualifierIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[LocationOfGoodsIdentification](
    page = IdentificationPage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.identification",
    findValueInDepartureData = message => {
      message.Consignment.LocationOfGoods.flatMap(
        value =>
          identificationTypes.find {
            i => value.qualifierOfIdentification == i.code
          }
      )
    },
    id = Some("change-qualifier-identification")
  )

  def authorisationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText(_),
    prefix = "locationOfGoods.authorisationNumber",
    findValueInDepartureData = message => message.Consignment.LocationOfGoods.flatMap(_.authorisationNumber),
    id = Some("change-authorisation-number")
  )
}
