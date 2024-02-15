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
import utils.transformer.liftToFuture
import utils.transformer.transport.border._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BorderMeansPipeline @Inject() (
  addAnotherBorderMeansOfTransportYesNoTransformer: AddAnotherBorderMeansOfTransportYesNoTransformer,
  addBorderMeansOfTransportYesNoTransformer: AddBorderMeansOfTransportYesNoTransformer,
  addConveyanceReferenceYesNoTransformer: AddConveyanceReferenceYesNoTransformer,
  conveyanceReferenceTransformer: ConveyanceReferenceTransformer,
  customsOfficeTransformer: CustomsOfficeTransformer,
  identificationTransformer: IdentificationTransformer,
  identificationNoTransformer: IdentificationNumberTransformer,
  nationalityTransformer: NationalityTransformer
)(implicit ec: ExecutionContext) {

  def pipeline(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    addAnotherBorderMeansOfTransportYesNoTransformer.transform andThen
      addBorderMeansOfTransportYesNoTransformer.transform andThen
      addConveyanceReferenceYesNoTransformer.transform andThen
      conveyanceReferenceTransformer.transform andThen
      customsOfficeTransformer.transform andThen
      identificationTransformer.transform andThen
      identificationNoTransformer.transform andThen
      nationalityTransformer.transform
}
