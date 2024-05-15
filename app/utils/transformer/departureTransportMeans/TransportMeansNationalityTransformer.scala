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

package utils.transformer.departureTransportMeans

import models.reference.Nationality
import models.{Index, UserAnswers}
import pages.transport.departureTransportMeans.TransportMeansNationalityPage
import services.NationalitiesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportMeansNationalityTransformer @Inject() (nationalitiesService: NationalitiesService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = Nationality
  override type ExtractedTypeInDepartureData = String

  def transform(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      fetchReferenceData = () => nationalitiesService.getNationalities().map(_.values),
      extractDataFromDepartureData = _.departureData.Consignment.DepartureTransportMeans.flatMap(_.nationality),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(
    departureDataNationalityCodes: Seq[String],
    nationalityList: Seq[Nationality]
  ): Seq[(TransportMeansNationalityPage, Nationality)] =
    departureDataNationalityCodes.zipWithIndex.collect {

      case (code, i) =>
        val index = Index(i)
        nationalityList
          .find(_.code == code)
          .map(
            nationality => (TransportMeansNationalityPage(index), nationality)
          )
    }.flatten

}
