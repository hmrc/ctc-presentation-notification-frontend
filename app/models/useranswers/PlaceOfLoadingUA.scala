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

package models.useranswers

import models.UserAnswers
import models.messages.PlaceOfLoading
import models.reference.Country
import pages.loading._
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class PlaceOfLoadingUA(
  addUnlocodeYesNo: Option[Boolean],
  unlocode: Option[String],
  addExtraInformationYesNo: Option[Boolean],
  country: Future[Option[Country]],
  location: Option[String]
)

object PlaceOfLoadingUA {

  def apply(
    ie170: UserAnswers,
    ie015: Option[PlaceOfLoading],
    checkYourAnswersReferenceDataService: CheckYourAnswersReferenceDataService
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): PlaceOfLoadingUA =
    (
      ie170.get(AddUnLocodeYesNoPage),
      ie170.get(UnLocodePage),
      ie170.get(AddExtraInformationYesNoPage),
      ie170.get(CountryPage),
      ie170.get(LocationPage)
    ) match {
      case (None, None, None, None, None) =>
        new PlaceOfLoadingUA(
          ie015.map(_.UNLocode.isDefined),
          ie015.flatMap(_.UNLocode),
          ie015 match {
            case Some(PlaceOfLoading(Some(_), Some(_), Some(_))) => Some(true)
            case Some(PlaceOfLoading(Some(_), None, None))       => Some(false)
            case _                                               => None
          },
          ie015.flatMap(_.country) match {
            case Some(code) => checkYourAnswersReferenceDataService.getCountry(code).map(Some(_))
            case None       => Future.successful(None)
          },
          ie015.flatMap(_.location)
        )
      case (addUnlocodeYesNo, unlocode, addExtraInformationYesNo, country, location) =>
        new PlaceOfLoadingUA(
          addUnlocodeYesNo,
          unlocode,
          addExtraInformationYesNo,
          Future.successful(country),
          location
        )
    }
}
