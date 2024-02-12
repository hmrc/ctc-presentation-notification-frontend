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
import utils.transformer.transport._
import utils.transformer.transport.border.{
  AddBorderModeOfTransportYesNoTransformer,
  IdentificationNumberTransformer,
  IdentificationTransformer,
  ModeOfTransportAtTheBorderTransformer
}
import utils.transformer.transport.border._
import utils.transformer.transport.equipment._
import utils.transformer.transport.placeOfLoading.{
  AddExtraInformationYesNoTransformer,
  AddUnLocodeYesNoTransformer,
  CountryTransformer,
  LocationTransformer,
  UnLocodeTransformer
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureDataTransformer @Inject() (
  addAnotherBorderMeansOfTransportYesNoTransformer: AddAnotherBorderMeansOfTransportYesNoTransformer,
  addBorderMeansOfTransportYesNoTransformer: AddBorderMeansOfTransportYesNoTransformer,
  addConveyanceReferenceYesNoTransformer: AddConveyanceReferenceYesNoTransformer,
  conveyanceReferenceTransformer: ConveyanceReferenceTransformer,
  customsOfficeTransformer: CustomsOfficeTransformer,
  identificationTransformer: IdentificationTransformer,
  identificationNoTransformer: IdentificationNumberTransformer,
  inlandModeTransformer: InlandModeTransformer,
  addInlandModeYesNoTransformer: AddInlandModeYesNoTransformer,
  nationalityTransformer: NationalityTransformer,
  transportEquipmentTransformer: TransportEquipmentTransformer,
  transportEquipmentYesNoTransformer: TransportEquipmentYesNoTransformer,
  containerIdTransformer: ContainerIdentificationNumberTransformer,
  containerIdentificationNumberYesNoTransformer: ContainerIdentificationNumberYesNoTransformer,
  sealTransformer: SealTransformer,
  addSealYesNoTransformer: AddSealYesNoTransformer,
  limitDateTransformer: LimitDateTransformer,
  itemTransformer: ItemTransformer,
  actingAsRepresentativeTransformer: ActingAsRepresentativeTransformer,
  representativeEoriTransformer: RepresentativeEoriTransformer,
  addRepresentativeContactDetailsYesNoTransformer: AddRepresentativeContactDetailsYesNoTransformer,
  representativeNameTransformer: RepresentativeNameTransformer,
  representativePhoneNumberTransformer: RepresentativePhoneNumberTransformer,
  containerIndicatorTransformer: ContainerIndicatorTransformer,
  modeOfTransportAtTheBorderTransformer: ModeOfTransportAtTheBorderTransformer,
  addBorderModeOfTransportYesNoTransformer: AddBorderModeOfTransportYesNoTransformer,
  transportMeansIdentificationTransformer: TransportMeansIdentificationTransformer,
  transportMeansIdentificationNumberTransformer: TransportMeansIdentificationNumberTransformer,
  transportMeansNationalityTransformer: TransportMeansNationalityTransformer
  addBorderModeOfTransportYesNoTransformer: AddBorderModeOfTransportYesNoTransformer,
  unLocodeTransformer: UnLocodeTransformer,
  addUnLocodeYesNoTransformer: AddUnLocodeYesNoTransformer,
  addExtraInformationYesNoTransformer: AddExtraInformationYesNoTransformer,
  locationTransformer: LocationTransformer,
  countryTransformer: CountryTransformer
)(implicit ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  def transform(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {

    val transportEquipmentPipeline =
      containerIdentificationNumberYesNoTransformer.transform andThen
        transportEquipmentYesNoTransformer.transform andThen
        transportEquipmentTransformer.transform andThen
        containerIdTransformer.transform andThen
        addSealYesNoTransformer.transform andThen
        sealTransformer.transform andThen
        itemTransformer.transform

    val borderMeansPipeline =
      addAnotherBorderMeansOfTransportYesNoTransformer.transform andThen
        addBorderMeansOfTransportYesNoTransformer.transform andThen
        addConveyanceReferenceYesNoTransformer.transform andThen
        conveyanceReferenceTransformer.transform andThen
        customsOfficeTransformer.transform andThen
        identificationTransformer.transform andThen
        identificationNoTransformer.transform andThen
        nationalityTransformer.transform

    val borderModePipeline =
      containerIndicatorTransformer.transform andThen
        addBorderModeOfTransportYesNoTransformer.transform andThen
        modeOfTransportAtTheBorderTransformer.transform

    val representativePipeline =
      addRepresentativeContactDetailsYesNoTransformer.transform andThen
        actingAsRepresentativeTransformer.transform andThen
        representativeEoriTransformer.transform andThen
        representativeNameTransformer.transform andThen
        representativePhoneNumberTransformer.transform

    val departureTransportMeansPipeline =
      addInlandModeYesNoTransformer.transform andThen
        inlandModeTransformer.transform andThen
        transportMeansIdentificationTransformer.transform andThen
        transportMeansIdentificationNumberTransformer.transform andThen
        transportMeansNationalityTransformer.transform

    val placeOfLoadingPipeline =
      addUnLocodeYesNoTransformer.transform andThen
        unLocodeTransformer.transform andThen
        addExtraInformationYesNoTransformer.transform andThen
        countryTransformer.transform andThen
        locationTransformer.transform

    val transformerPipeline =
      borderModePipeline andThen
        borderMeansPipeline andThen
        departureTransportMeansPipeline andThen
        transportEquipmentPipeline andThen
        limitDateTransformer.transform andThen
        representativePipeline andThen
        placeOfLoadingPipeline

    transformerPipeline(userAnswers)
  }

}
