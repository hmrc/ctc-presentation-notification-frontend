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

import generated.ActiveBorderTransportMeansType03
import models.UserAnswers
import pages.transport.border.AddBorderMeansOfTransportYesNoPage
import pages.transport.border.active.*
import services.{CheckYourAnswersReferenceDataService, CustomsOfficesService, MeansOfTransportIdentificationTypesActiveService}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ActiveBorderTransportMeansTransformer @Inject() (
  customsReferenceDataService: CustomsOfficesService,
  identificationService: MeansOfTransportIdentificationTypesActiveService,
  nationalityService: CheckYourAnswersReferenceDataService
)(implicit ec: ExecutionContext)
    extends NewPageTransformer {

  def transform(
    activeBorderTransportMeans: Seq[ActiveBorderTransportMeansType03]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    set(AddBorderMeansOfTransportYesNoPage, activeBorderTransportMeans.nonEmpty) andThen
      activeBorderTransportMeans.mapWithSets {
        (value, index) =>
          set(CustomsOfficeActiveBorderPage(index), value.customsOfficeAtBorderReferenceNumber, customsReferenceDataService.getCustomsOfficeById) andThen
            set(IdentificationPage(index), value.typeOfIdentification, identificationService.getBorderMeansIdentification) andThen
            set(IdentificationNumberPage(index), value.identificationNumber) andThen
            set(NationalityPage(index), value.nationality, nationalityService.getNationality) andThen
            set(AddConveyanceReferenceYesNoPage(index), value.conveyanceReferenceNumber.isDefined) andThen
            set(ConveyanceReferenceNumberPage(index), value.conveyanceReferenceNumber)
      }
}
