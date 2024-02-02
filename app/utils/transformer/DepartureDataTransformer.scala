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
import utils.transformer.transport.LimitDateTransformer
import utils.transformer.transport.border._
import utils.transformer.transport.equipment._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureDataTransformer @Inject() (
  identificationTransformer: IdentificationTransformer,
  identificationNoTransformer: IdentificationNumberTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  transportEquipmentYesNoTransformer: TransportEquipmentYesNoTransformer,
  containerIdTransformer: ContainerIdentificationNumberTransformer,
  containerIdentificationNumberYesNoTransformer: ContainerIdentificationNumberYesNoTransformer,
  sealTransformer: SealTransformer,
  addSealYesNoTransformer: AddSealYesNoTransformer,
  limitDateTransformer: LimitDateTransformer,
  itemTransformer: ItemTransformer,
  containerIndicatorTransformer: ContainerIndicatorTransformer,
  modeOfTransportAtTheBorderTransformer: ModeOfTransportAtTheBorderTransformer,
  addBorderModeOfTransportYesNoTransformer: AddBorderModeOfTransportYesNoTransformer
)(implicit ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  def transform(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {

    val transformerPipeline = identificationTransformer.transform andThen
      identificationNoTransformer.transform andThen
      containerIndicatorTransformer.transform andThen
      transportEquipmentYesNoTransformer.transform andThen
      transportEquipmentTransformer.transform andThen
      containerIdentificationNumberYesNoTransformer.transform andThen
      containerIdTransformer.transform andThen
      addSealYesNoTransformer.transform andThen
      sealTransformer.transform andThen
      limitDateTransformer.transform andThen
      itemTransformer.transform andThen
      addBorderModeOfTransportYesNoTransformer.transform andThen
      modeOfTransportAtTheBorderTransformer.transform
    transformerPipeline(userAnswers)
  }

}
