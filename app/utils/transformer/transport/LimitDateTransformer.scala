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

package utils.transformer.transport

import models.{xmlGregorianCalendarToLocalDate, UserAnswers}
import pages.transport.LimitDatePage
import uk.gov.hmrc.http.HeaderCarrier
import utils.transformer.PageTransformer

import java.time.LocalDate
import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.Future

class LimitDateTransformer extends PageTransformer {

  override type DomainModelType              = LocalDate
  override type ExtractedTypeInDepartureData = XMLGregorianCalendar

  override def transform(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transformFromDeparture(
      userAnswers = userAnswers,
      extractDataFromDepartureData = _.departureData.TransitOperation.limitDate.toSeq,
      generateCapturedAnswers = limitDates =>
        limitDates.map(
          limitDate => (LimitDatePage, limitDate)
        )
    )
}
