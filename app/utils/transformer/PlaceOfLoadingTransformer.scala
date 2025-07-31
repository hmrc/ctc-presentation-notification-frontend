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

import generated.PlaceOfLoadingType
import models.UserAnswers
import pages.loading.{AddExtraInformationYesNoPage, AddUnLocodeYesNoPage, CountryPage, LocationPage, UnLocodePage}
import services.CountriesService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlaceOfLoadingTransformer @Inject() (
  countryService: CountriesService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    placeOfLoading: Option[PlaceOfLoadingType]
  )(implicit headerCarrier: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    placeOfLoading.mapWithSets {
      value =>
        set(AddUnLocodeYesNoPage, value.UNLocode.isDefined) andThen
          set(UnLocodePage, value.UNLocode) andThen
          setAddExtraInformationYesNoPage(value) andThen
          set(CountryPage, value.country, countryService.getCountry) andThen
          set(LocationPage, value.location)

    }

  private def setAddExtraInformationYesNoPage(placeOfLoading: PlaceOfLoadingType): UserAnswers => Future[UserAnswers] =
    if (placeOfLoading.UNLocode.isDefined) {
      set(AddExtraInformationYesNoPage, placeOfLoading.country.isDefined)
    } else {
      Future.successful
    }

}
