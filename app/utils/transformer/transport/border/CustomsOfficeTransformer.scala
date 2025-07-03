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

import models.reference.CustomsOffice
import models.{Index, UserAnswers}
import pages.transport.border.active.CustomsOfficeActiveBorderPage
import services.CustomsOfficesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeTransformer @Inject() (service: CustomsOfficesService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = CustomsOffice
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform: UserAnswers => Boolean = _.departureData.Consignment.ActiveBorderTransportMeans.nonEmpty

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      extractDataFromDepartureData = extractDataFromDepartureData,
      fetchReferenceData = () => service.getCustomsOfficesByMultipleIds(extractDataFromDepartureData(userAnswers)),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(departureDataCustomsOfficeRefs: Seq[String], customsOffices: Seq[CustomsOffice]): Seq[CapturedAnswer] =
    departureDataCustomsOfficeRefs.zipWithIndex.flatMap {
      case (ref, i) =>
        val index = Index(i)
        customsOffices
          .find(_.id == ref)
          .map((CustomsOfficeActiveBorderPage(index), _))
    }

  private def extractDataFromDepartureData: UserAnswers => Seq[ExtractedTypeInDepartureData] =
    _.departureData.Consignment.ActiveBorderTransportMeans.map(_.customsOfficeAtBorderReferenceNumber)
}
