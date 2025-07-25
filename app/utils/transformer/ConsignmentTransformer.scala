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

import generated.ConsignmentType23
import models.UserAnswers
import pages.transport.{AddInlandModeOfTransportYesNoPage, ContainerIndicatorPage, InlandModePage}
import services.TransportModeCodesService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentTransformer @Inject() (
  departureTransportMeansTransformer: DepartureTransportMeansTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  transportModeCodesService: TransportModeCodesService
)(implicit ec: ExecutionContext)
    extends NewPageTransformer {

  def transform(consignment: ConsignmentType23)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    set(AddInlandModeOfTransportYesNoPage, consignment.inlandModeOfTransport.isDefined) andThen
      set(InlandModePage, consignment.inlandModeOfTransport, transportModeCodesService.getInlandMode) andThen
      set(ContainerIndicatorPage, consignment.containerIndicator.map(_.toBoolean)) andThen
      departureTransportMeansTransformer.transform(consignment.DepartureTransportMeans) andThen
      transportEquipmentTransformer.transform(consignment.TransportEquipment)
}
