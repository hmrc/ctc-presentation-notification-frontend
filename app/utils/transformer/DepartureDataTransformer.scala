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

package utils.transformer

import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import utils.transformer.pipeline._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureDataTransformer @Inject() (
  borderMeansPipeline: BorderMeansPipeline,
  borderModePipeline: BorderModePipeline,
  departureTransportMeansPipeline: DepartureTransportMeansPipeline,
  locationOfGoodsPipeline: LocationOfGoodsPipeline,
  placeOfLoadingPipeline: PlaceOfLoadingPipeline,
  representativePipeline: RepresentativePipeline,
  transportPipeline: TransportPipeline,
  transportEquipmentPipeline: TransportEquipmentPipeline
)(implicit ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  def transform(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {

    val transformerPipeline =
      transportPipeline.pipeline andThen
        borderModePipeline.pipeline andThen
        borderMeansPipeline.pipeline andThen
        departureTransportMeansPipeline.pipeline andThen
        locationOfGoodsPipeline.pipeline andThen
        placeOfLoadingPipeline.pipeline andThen
        representativePipeline.pipeline andThen
        transportEquipmentPipeline.pipeline

    transformerPipeline(userAnswers)
  }

}
