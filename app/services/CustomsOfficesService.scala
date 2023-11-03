/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import connectors.ReferenceDataConnector
import models.{RichOptionalJsArray, SelectableList, UserAnswers}
import models.SelectableList.{officeOfDestinationReads, officesOfExitReads, officesOfTransitReads}
import models.reference.{CountryCode, CustomsOffice}
import pages.sections.external.{OfficeOfDestinationSection, OfficesOfExitSection, OfficesOfTransitSection}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficesService @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext) {

  def getCustomsOfficesOfTransitForCountry(
    countryCode: CountryCode
  )(implicit hc: HeaderCarrier): Future[SelectableList[CustomsOffice]] =
    referenceDataConnector
      .getCustomsOfficesOfTransitForCountry(countryCode)
      .map(sort)

  def getCustomsOfficeById(id: String)(implicit hc: HeaderCarrier): Future[Option[CustomsOffice]] =
    referenceDataConnector.getCustomsOfficesForId(id).map(_.headOption)

  def getCustomsOfficesOfDestinationForCountry(
    countryCode: CountryCode
  )(implicit hc: HeaderCarrier): Future[SelectableList[CustomsOffice]] =
    referenceDataConnector
      .getCustomsOfficesOfDestinationForCountry(countryCode)
      .map(sort)

  def getCustomsOfficesOfExitForCountry(
    countryCode: CountryCode
  )(implicit hc: HeaderCarrier): Future[SelectableList[CustomsOffice]] =
    referenceDataConnector
      .getCustomsOfficesOfExitForCountry(countryCode)
      .map(sort)

  def getCustomsOfficesOfDepartureForCountry(
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[SelectableList[CustomsOffice]] =
    referenceDataConnector
      .getCustomsOfficesOfDepartureForCountry(countryCode)
      .map(sort)

  def getCustomsOffices(userAnswers: UserAnswers): SelectableList[CustomsOffice] = {
    val officesOfExit       = userAnswers.get(OfficesOfExitSection).validate(officesOfExitReads).map(_.values).getOrElse(Nil)
    val officesOfTransit    = userAnswers.get(OfficesOfTransitSection).validate(officesOfTransitReads).map(_.values).getOrElse(Nil)
    val officeOfDestination = userAnswers.get(OfficeOfDestinationSection).validate(officeOfDestinationReads).map(_.values).getOrElse(Nil)

    sort(officesOfExit ++ officesOfTransit ++ officeOfDestination)
  }

  private def sort(customsOffices: Seq[CustomsOffice]): SelectableList[CustomsOffice] =
    SelectableList(customsOffices.sortBy(_.name.toLowerCase))

}
