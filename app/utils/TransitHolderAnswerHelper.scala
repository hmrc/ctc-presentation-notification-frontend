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

package utils

import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import services.CheckYourAnswersReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.Section

import scala.concurrent.{ExecutionContext, Future}

class TransitHolderAnswerHelper(
  userAnswers: UserAnswers,
  departureId: String,
  cyaRefDataService: CheckYourAnswersReferenceDataService,
  mode: Mode
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends AnswersHelper(userAnswers, departureId, mode) {

  def eoriYesNoRow: SummaryListRow =
    buildRowWithNoChangeLink(
      prefix = "transitHolder.eoriYesNo",
      answer = formatAsYesOrNo(userAnswers.departureData.HolderOfTheTransitProcedure.identificationNumber.isDefined)
    )

  def eoriRow: Option[SummaryListRow] = userAnswers.departureData.HolderOfTheTransitProcedure.identificationNumber.map {
    eori =>
      buildRowWithNoChangeLink(
        prefix = "transitHolder.eori",
        answer = formatAsText(eori)
      )
  }

  def nameRow: Option[SummaryListRow] = userAnswers.departureData.HolderOfTheTransitProcedure.ContactPerson.map {
    person =>
      buildRowWithNoChangeLink(
        prefix = "transitHolder.name",
        answer = formatAsText(person.name)
      )
  }

  def countryRow: Option[Future[SummaryListRow]] = userAnswers.departureData.HolderOfTheTransitProcedure.Address.map {
    address =>
      cyaRefDataService
        .getCountry(address.country)
        .map(
          country =>
            buildRowWithNoChangeLink(
              prefix = "transitHolder.country",
              answer = formatAsCountry(country)
            )
        )
  }

  def addressRow: Option[SummaryListRow] = userAnswers.departureData.HolderOfTheTransitProcedure.Address.map {
    address =>
      buildRowWithNoChangeLink(
        prefix = "transitHolder.address",
        answer = formatAsDynamicAddress(address.toDynamicAddress)
      )
  }

  def tirIdentificationRow: Option[SummaryListRow] = userAnswers.departureData.HolderOfTheTransitProcedure.TIRHolderIdentificationNumber.map {
    tirIdentification =>
      buildRowWithNoChangeLink(
        prefix = "transitHolder.tirIdentification",
        answer = formatAsText(tirIdentification)
      )
  }

  def transitHolderSection: Future[Section] =
    for {
      eoriYesNoRow         <- Future.successful(Some(eoriYesNoRow))
      eoriRow              <- eoriRow.toFuture
      nameRow              <- nameRow.toFuture
      countryRow           <- countryRow.toFuture
      addressRow           <- addressRow.toFuture
      tirIdentificationRow <- tirIdentificationRow.toFuture
    } yield Section(
      sectionTitle = messages("checkYourAnswers.transitHolder"),
      Seq(eoriYesNoRow, eoriRow, nameRow, countryRow, addressRow, tirIdentificationRow).flatten
    )
}
