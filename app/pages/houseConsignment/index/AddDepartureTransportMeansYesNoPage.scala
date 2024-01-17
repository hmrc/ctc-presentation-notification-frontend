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

package pages.houseConsignment.index

import controllers.houseConsignment.index.routes
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.houseConsignment.HouseConsignmentSection
import pages.sections.houseConsignment.departureTransportMeans.DepartureTransportMeansListSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class AddDepartureTransportMeansYesNoPage(houseConsignmentIndex: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = HouseConsignmentSection(houseConsignmentIndex).path \ toString

  override def toString: String = "addDepartureTransportMeansYesNo"

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(routes.AddDepartureTransportMeansYesNoController.onPageLoad(departureId, mode, houseConsignmentIndex))

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    val ie015HCDepartureTransportMeansPath: JsPath = JsPath \ "Consignment" \ "HouseConsignment" \ houseConsignmentIndex.position \ "DepartureTransportMeans"
    value match {
      case Some(false) => userAnswers.remove(DepartureTransportMeansListSection(houseConsignmentIndex), ie015HCDepartureTransportMeansPath)
      case _           => super.cleanup(value, userAnswers)
    }
  }
}
