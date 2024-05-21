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

import models.{LocationOfGoodsIdentification, UserAnswers}
import pages.locationOfGoods.{BaseIdentificationPage, IdentificationPage, InferredIdentificationPage}
import services.LocationOfGoodsIdentificationTypeService
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import javax.inject.Inject
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class IdentificationTransformer @Inject() (service: LocationOfGoodsIdentificationTypeService)(implicit
  ec: ExecutionContext
) extends PageTransformer {

  override type DomainModelType              = LocationOfGoodsIdentification
  override type ExtractedTypeInDepartureData = String
  override def shouldTransform = _.departureData.Consignment.LocationOfGoods.map(_.qualifierOfIdentification).isDefined

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDepartureWithRefData(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.LocationOfGoods.map(_.qualifierOfIdentification).toList,
      fetchReferenceData = () => getLocationType(userAnswers).map(service.getLocationOfGoodsIdentificationTypes).getOrElse(successful(Seq())),
      generateCapturedAnswers = generateCapturedAnswers
    )

  private def generateCapturedAnswers(
    departureIdentificationTypes: Seq[String],
    refDataIdentificationTypes: Seq[LocationOfGoodsIdentification]
  ): Seq[CapturedAnswer] =
    departureIdentificationTypes.flatMap {
      departureIdentificationType =>
        val page: BaseIdentificationPage = refDataIdentificationTypes.toList match {
          case _ :: Nil => InferredIdentificationPage
          case _        => IdentificationPage
        }
        refDataIdentificationTypes
          .find(_.code == departureIdentificationType)
          .map((page, _))
    }

  private def getLocationType(userAnswers: UserAnswers): Option[String] =
    userAnswers.departureData.Consignment.LocationOfGoods.map(_.typeOfLocation)

}
