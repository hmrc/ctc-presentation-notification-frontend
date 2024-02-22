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

import base.SpecBase
import models.UserAnswers
import org.mockito.Mockito.{times, verify, when}
import utils.transformer.Helper.verifyTransformer
import utils.transformer.locationOfGoods._

import scala.concurrent.ExecutionContext.Implicits.global

class LocationOfGoodsPipelineTest extends SpecBase {

  "LocationOfGoodsPipeline" - {
    "should call all transformers" in {

      val addContactYesNoTransformer               = mock[AddContactYesNoTransformer]
      val addIdentifierYesNoTransformer            = mock[AddIdentifierYesNoTransformer]
      val additionalIdentifierTransformer          = mock[AdditionalIdentifierTransformer]
      val addressTransformer                       = mock[AddressTransformer]
      val authorisationNumberTransformer           = mock[AuthorisationNumberTransformer]
      val coordinatesTransformer                   = mock[CoordinatesTransformer]
      val countryTransformer                       = mock[CountryTransformer]
      val customsOfficeIdentifierTransformer       = mock[CustomsOfficeIdentifierTransformer]
      val eoriTransformer                          = mock[EoriTransformer]
      val locationOfGoodsIdentificationTransformer = mock[IdentificationTransformer]
      val locationTypeTransformer                  = mock[LocationTypeTransformer]
      val nameTransformer                          = mock[NameTransformer]
      val phoneNumberTransformer                   = mock[PhoneNumberTransformer]
      val postalCodeTransformer                    = mock[PostalCodeTransformer]
      val unLocodeTransformer                      = mock[UnLocodeTransformer]

      val userAnswers                   = mock[UserAnswers]
      val uaWithLocationType            = mock[UserAnswers]
      val uaWithIdentification          = mock[UserAnswers]
      val uaWithCustomsOfficeIdentifier = mock[UserAnswers]
      val uaWithAddContactYesNo         = mock[UserAnswers]
      val uaWithName                    = mock[UserAnswers]
      val uaWithPhoneNumber             = mock[UserAnswers]
      val uaWithCountry                 = mock[UserAnswers]
      val uaWithAddress                 = mock[UserAnswers]
      val uaWithPostalCode              = mock[UserAnswers]
      val uaWithCoordinates             = mock[UserAnswers]
      val uaWithAddIdentifierYesNo      = mock[UserAnswers]
      val uaWithAdditionalIdentifier    = mock[UserAnswers]
      val uaWithAuthorisationNumber     = mock[UserAnswers]
      val uaWithEori                    = mock[UserAnswers]

      //Ensure the order of these transformers otherwise cleanup logic after setting page will cause loosing some answers
      when(locationTypeTransformer.transform(hc)).thenReturn(verifyTransformer(expect = userAnswers, `return` = uaWithLocationType))
      when(locationOfGoodsIdentificationTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithLocationType, `return` = uaWithIdentification))
      when(customsOfficeIdentifierTransformer.transform(hc))
        .thenReturn(verifyTransformer(expect = uaWithIdentification, `return` = uaWithCustomsOfficeIdentifier))
      when(addContactYesNoTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithCustomsOfficeIdentifier, `return` = uaWithAddContactYesNo))
      when(nameTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithAddContactYesNo, `return` = uaWithName))
      when(phoneNumberTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithName, `return` = uaWithPhoneNumber))
      when(countryTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithPhoneNumber, `return` = uaWithCountry))
      when(addressTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithCountry, `return` = uaWithAddress))
      when(postalCodeTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithAddress, `return` = uaWithPostalCode))
      when(coordinatesTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithPostalCode, `return` = uaWithCoordinates))
      when(addIdentifierYesNoTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithCoordinates, `return` = uaWithAddIdentifierYesNo))
      when(additionalIdentifierTransformer.transform(hc))
        .thenReturn(verifyTransformer(expect = uaWithAddIdentifierYesNo, `return` = uaWithAdditionalIdentifier))
      when(authorisationNumberTransformer.transform(hc))
        .thenReturn(verifyTransformer(expect = uaWithAdditionalIdentifier, `return` = uaWithAuthorisationNumber))
      when(eoriTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithAuthorisationNumber, `return` = uaWithEori))
      when(unLocodeTransformer.transform(hc)).thenReturn(verifyTransformer(expect = uaWithEori, `return` = userAnswers))

      val pipeline = new LocationOfGoodsPipeline(
        addContactYesNoTransformer,
        addIdentifierYesNoTransformer,
        additionalIdentifierTransformer,
        addressTransformer,
        authorisationNumberTransformer,
        coordinatesTransformer,
        countryTransformer,
        customsOfficeIdentifierTransformer,
        eoriTransformer,
        locationOfGoodsIdentificationTransformer,
        locationTypeTransformer,
        nameTransformer,
        phoneNumberTransformer,
        postalCodeTransformer,
        unLocodeTransformer
      )

      pipeline.pipeline(hc)(userAnswers)

      verify(addContactYesNoTransformer, times(1)).transform(hc)
      verify(addIdentifierYesNoTransformer, times(1)).transform(hc)
      verify(additionalIdentifierTransformer, times(1)).transform(hc)
      verify(addressTransformer, times(1)).transform(hc)
      verify(authorisationNumberTransformer, times(1)).transform(hc)
      verify(coordinatesTransformer, times(1)).transform(hc)
      verify(countryTransformer, times(1)).transform(hc)
      verify(customsOfficeIdentifierTransformer, times(1)).transform(hc)
      verify(eoriTransformer, times(1)).transform(hc)
      verify(locationOfGoodsIdentificationTransformer, times(1)).transform(hc)
      verify(locationTypeTransformer, times(1)).transform(hc)
      verify(nameTransformer, times(1)).transform(hc)
      verify(phoneNumberTransformer, times(1)).transform(hc)
      verify(postalCodeTransformer, times(1)).transform(hc)
      verify(unLocodeTransformer, times(1)).transform(hc)
    }
  }
}
