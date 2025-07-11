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

package utils.transformer.transport.border

import generated.ConsignmentType23
import models.UserAnswers
import models.reference.TransportMode.BorderMode
import pages.transport.border.BorderModeOfTransportPage
import services.TransportModeCodesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ModeOfTransportAtTheBorderTransformer @Inject() (borderModeService: TransportModeCodesService)(implicit ec: ExecutionContext) extends PageTransformer {
  override type DomainModelType              = BorderMode
  override type ExtractedTypeInDepartureData = ConsignmentType23

  private def generateCapturedAnswers(
    departureDataCode: Seq[ConsignmentType23],
    borderModeList: Seq[BorderMode]
  ): Seq[(BorderModeOfTransportPage.type, BorderMode)] =
    departureDataCode.flatMap(
      consignment =>
        consignment.modeOfTransportAtTheBorder
          .flatMap(
            departureDataMode => borderModeList.find(_.code == departureDataMode)
          )
          .map(
            borderMode => (BorderModeOfTransportPage, borderMode)
          )
    )

  def transform(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      fetchReferenceData = () => borderModeService.getBorderModes(),
      extractDataFromDepartureData = userAnswers => Seq(userAnswers.departureData.Consignment),
      generateCapturedAnswers = generateCapturedAnswers
    )

}
