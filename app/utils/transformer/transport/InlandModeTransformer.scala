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

package utils.transformer.transport

import models.UserAnswers
import models.reference.TransportMode.InlandMode
import pages.transport.InlandModePage
import services.TransportModeCodesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InlandModeTransformer @Inject() (transportModeCodesService: TransportModeCodesService)(implicit ec: ExecutionContext) extends PageTransformer {

  override type DomainModelType              = InlandMode
  override type ExtractedTypeInDepartureData = String

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      fetchReferenceData = () => transportModeCodesService.getInlandModes(),
      extractDataFromDepartureData = _.departureData.Consignment.inlandModeOfTransport.toSeq,
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(inlandModeCodes: Seq[String], inlandModeList: Seq[InlandMode]): Seq[(InlandModePage.type, InlandMode)] =
    inlandModeCodes.flatMap {
      code =>
        inlandModeList
          .find(_.code == code)
          .map(
            inlandMode => (InlandModePage, inlandMode)
          )
    }

}
