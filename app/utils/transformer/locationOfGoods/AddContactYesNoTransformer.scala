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

package utils.transformer.locationOfGoods

import config.Constants.QualifierOfTheIdentification._
import generated.ContactPersonType06
import models.UserAnswers
import pages.locationOfGoods.{AddContactYesNoPage, IdentificationPage}
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import scala.concurrent.Future

class AddContactYesNoTransformer extends PageTransformer {
  override type DomainModelType              = Boolean
  override type ExtractedTypeInDepartureData = ContactPersonType06

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDeparture(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.Consignment.LocationOfGoods.flatMap(_.ContactPerson).toSeq,
      generateCapturedAnswers = contactPerson =>
        if (userAnswers.get(IdentificationPage).map(_.code).contains(CustomsOfficeIdentifier)) {
          Seq()
        } else {
          Seq((AddContactYesNoPage, contactPerson.nonEmpty))
        }
    )
}
