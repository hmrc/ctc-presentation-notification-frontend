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

import models.reference.transport.border.active.Identification
import models.{Index, UserAnswers}
import pages.transport.border.active.IdentificationPage
import services.MeansOfTransportIdentificationTypesActiveService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationTransformer @Inject() (identificationService: MeansOfTransportIdentificationTypesActiveService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = Identification
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform = _.departureData.Consignment.ActiveBorderTransportMeans.toList.flatten.nonEmpty

  def transform(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      fetchReferenceData = () => identificationService.getMeansOfTransportIdentificationTypesActive(),
      extractDataFromDepartureData = _.departureData.Consignment.ActiveBorderTransportMeans.toList.flatten.flatMap(_.typeOfIdentification),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(departureDataIdentificationCodes: Seq[String], identificationList: Seq[Identification]): Seq[CapturedAnswer] =
    departureDataIdentificationCodes.zipWithIndex.flatMap {
      case (code, i) =>
        val index = Index(i)
        identificationList
          .find(_.code == code)
          .map((IdentificationPage(index), _))
    }
}
