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

import models.{PostalCodeAddress, RichPostcodeAddressType02, UserAnswers}
import pages.locationOfGoods.PostalCodePage
import services.CountriesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostalCodeTransformer @Inject() (countriesService: CountriesService)(implicit ec: ExecutionContext) extends PageTransformer {
  override type DomainModelType              = PostalCodeAddress
  override type ExtractedTypeInDepartureData = PostalCodeAddress

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    countriesService.getCountries().flatMap {
      countryList =>
        transformFromDeparture(
          userAnswers = userAnswers,
          extractDataFromDepartureData = userAnswers =>
            (for {
              locationOfGoods <- userAnswers.departureData.Consignment.LocationOfGoods
              address         <- locationOfGoods.PostcodeAddress
              country         <- countryList.values.find(_.code.code == address.country)
            } yield address.toPostalCode(country)).toSeq,
          generateCapturedAnswers = _.map((PostalCodePage, _))
        )
    }
}
