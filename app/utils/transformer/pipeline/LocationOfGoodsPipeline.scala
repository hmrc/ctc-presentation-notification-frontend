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
import utils.transformer.locationOfGoods._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsPipeline @Inject() (
  addContactYesNoTransformer: AddContactYesNoTransformer,
  addIdentifierYesNoTransformer: AddIdentifierYesNoTransformer,
  additionalIdentifierTransformer: AdditionalIdentifierTransformer,
  addressTransformer: AddressTransformer,
  authorisationNumberTransformer: AuthorisationNumberTransformer,
  coordinatesTransformer: CoordinatesTransformer,
  countryTransformer: CountryTransformer,
  customsOfficeIdentifierTransformer: CustomsOfficeIdentifierTransformer,
  eoriTransformer: EoriTransformer,
  locationOfGoodsIdentificationTransformer: IdentificationTransformer,
  locationTypeTransformer: LocationTypeTransformer,
  nameTransformer: NameTransformer,
  phoneNumberTransformer: PhoneNumberTransformer,
  postalCodeTransformer: PostalCodeTransformer,
  unLocodeTransformer: UnLocodeTransformer
)(implicit ec: ExecutionContext) {

  def pipeline(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    locationTypeTransformer.transform andThen
      locationOfGoodsIdentificationTransformer.transform andThen
      customsOfficeIdentifierTransformer.transform andThen
      addContactYesNoTransformer.transform andThen
      nameTransformer.transform andThen
      phoneNumberTransformer.transform andThen
      countryTransformer.transform andThen
      addressTransformer.transform andThen
      postalCodeTransformer.transform andThen
      coordinatesTransformer.transform andThen
      addIdentifierYesNoTransformer.transform andThen
      additionalIdentifierTransformer.transform andThen
      authorisationNumberTransformer.transform andThen
      eoriTransformer.transform andThen
      unLocodeTransformer.transform
}
