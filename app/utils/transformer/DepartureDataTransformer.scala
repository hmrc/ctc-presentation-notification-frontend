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
import utils.transformer.representative._
import utils.transformer.transport.LimitDateTransformer
import utils.transformer.transport.border.{
  AddBorderModeOfTransportYesNoTransformer,
  IdentificationNumberTransformer,
  IdentificationTransformer,
  ModeOfTransportAtTheBorderTransformer
}
import utils.transformer.transport.equipment.{
  ContainerIdentificationNumberTransformer,
  ContainerIndicatorTransformer,
  SealTransformer,
  TransportEquipmentTransformer
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureDataTransformer @Inject() (
  identificationTransformer: IdentificationTransformer,
  identificationNoTransformer: IdentificationNumberTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  containerIdTransformer: ContainerIdentificationNumberTransformer,
  sealTransformer: SealTransformer,
  limitDateTransformer: LimitDateTransformer,
  actingAsRepresentativeTransformer: ActingAsRepresentativeTransformer,
  representativeEoriTransformer: RepresentativeEoriTransformer,
  addRepresentativeContactDetailsYesNoTransformer: AddRepresentativeContactDetailsYesNoTransformer,
  representativeNameTransformer: RepresentativeNameTransformer,
  representativePhoneNumberTransformer: RepresentativePhoneNumberTransformer,
  containerIndicatorTransformer: ContainerIndicatorTransformer,
  modeOfTransportAtTheBorderTransformer: ModeOfTransportAtTheBorderTransformer,
  addBorderModeOfTransportYesNoTransformer: AddBorderModeOfTransportYesNoTransformer
)(implicit ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  def transform(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {

    val transformerPipeline = identificationTransformer.transform andThen
      identificationNoTransformer.transform andThen
      transportEquipmentTransformer.transform andThen
      containerIdTransformer.transform andThen
      sealTransformer.transform andThen
      limitDateTransformer.transform andThen
      addRepresentativeContactDetailsYesNoTransformer.transform andThen
      actingAsRepresentativeTransformer.transform andThen
      representativeEoriTransformer.transform andThen
      representativeNameTransformer.transform andThen
      representativePhoneNumberTransformer.transform andThen
      containerIndicatorTransformer.transform andThen
      modeOfTransportAtTheBorderTransformer.transform andThen
      addBorderModeOfTransportYesNoTransformer.transform

    transformerPipeline(userAnswers)
  }

}
