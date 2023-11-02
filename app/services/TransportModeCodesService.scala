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

import config.Constants._
import connectors.ReferenceDataConnector
import models.reference.{BorderMode, ModeOfTransport}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportModeCodesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getBorderModes()(implicit hc: HeaderCarrier): Future[Seq[BorderMode]] =
    referenceDataConnector
      .getTransportModeCodes[BorderMode]()
      .map(filterBorderModes)
      .map(sort)

  private def filterBorderModes(
    borderModeTypes: Seq[BorderMode]
  ): Seq[BorderMode] = {
    val agreedBorderModes = Seq(Maritime, Rail, Road, Air)

    borderModeTypes.filter(
      mode => agreedBorderModes.contains(mode.code)
    )
  }

  private def sort[T <: ModeOfTransport[T]](transportModeCodes: Seq[T]): Seq[T] =
    transportModeCodes.sortBy(_.code.toLowerCase)
}
