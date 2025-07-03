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

import models.reference.CustomsOffice
import models.{RichCC015CType, UserAnswers}
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import services.CustomsOfficesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeIdentifierTransformer @Inject() (service: CustomsOfficesService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = CustomsOffice
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform: UserAnswers => Boolean = _.departureData.Consignment.LocationOfGoods.flatMap(_.CustomsOffice).isDefined

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.LocationOfGoods.flatMap(_.CustomsOffice.map(_.referenceNumber)).toList,
      fetchReferenceData = () => service.getCustomsOfficesOfDepartureForCountry(userAnswers.departureData.countryOfDeparture).map(_.values),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(departureCustomsOffices: Seq[String], refDataCustomsOffices: Seq[CustomsOffice]): Seq[CapturedAnswer] =
    departureCustomsOffices.flatMap {
      departureCustomsOffice =>
        refDataCustomsOffices
          .find(
            co => departureCustomsOffice.contains(co.id)
          )
          .map((CustomsOfficeIdentifierPage, _))
    }

}
