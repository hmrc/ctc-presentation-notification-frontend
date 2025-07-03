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

package utils.transformer.locationOfGoods

import models.UserAnswers
import models.reference.Country
import pages.locationOfGoods.CountryPage
import services.CountriesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryTransformer @Inject() (service: CountriesService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = Country
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform: UserAnswers => Boolean = _.departureData.Consignment.LocationOfGoods.flatMap(_.Address.map(_.country)).isDefined

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.LocationOfGoods.flatMap(_.Address.map(_.country)).toList,
      fetchReferenceData = () => service.getCountries().map(_.values),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(departureCountries: Seq[String], refDataCountries: Seq[Country]): Seq[CapturedAnswer] =
    departureCountries.flatMap {
      departureCountry =>
        refDataCountries
          .find(_.code.code == departureCountry)
          .map((CountryPage, _))
    }

}
