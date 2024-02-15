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

package utils.transformer.pipeline

import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.departureTransportMeans.{
  TransportMeansIdentificationNumberTransformer,
  TransportMeansIdentificationTransformer,
  TransportMeansNationalityTransformer
}
import utils.transformer.liftToFuture

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureTransportMeansPipeline @Inject() (
  transportMeansIdentificationTransformer: TransportMeansIdentificationTransformer,
  transportMeansIdentificationNumberTransformer: TransportMeansIdentificationNumberTransformer,
  transportMeansNationalityTransformer: TransportMeansNationalityTransformer
)(implicit ec: ExecutionContext) {

  def pipeline(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    transportMeansIdentificationTransformer.transform andThen
      transportMeansIdentificationNumberTransformer.transform andThen
      transportMeansNationalityTransformer.transform
}
