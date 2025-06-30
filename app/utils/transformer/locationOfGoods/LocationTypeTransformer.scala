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

import models.reference.LocationType
import models.{RichCC015CType, UserAnswers}
import pages.locationOfGoods.{BaseLocationTypePage, InferredLocationTypePage, LocationTypePage}
import services.LocationTypeService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationTypeTransformer @Inject() (service: LocationTypeService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = LocationType
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform = _.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation).isDefined

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation).toList,
      fetchReferenceData = () => service.getLocationTypes(userAnswers.departureData.isSimplified),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(departureLocationTypes: Seq[String], refDataLocationTypes: Seq[LocationType]): Seq[CapturedAnswer] =
    departureLocationTypes.flatMap {
      departureLocationType =>
        val page: BaseLocationTypePage = refDataLocationTypes.toList match {
          case _ :: Nil => InferredLocationTypePage
          case _        => LocationTypePage
        }
        refDataLocationTypes
          .find(_.code == departureLocationType)
          .map((page, _))
    }

}
