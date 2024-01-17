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

package navigation

import com.google.inject.Singleton
import models.{CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.houseConsignment.index.AddDepartureTransportMeansYesNoPage
import pages.houseConsignment.index.departureTransportMeans.{CountryPage, IdentificationNumberPage, IdentificationPage}
import pages.sections.houseConsignment.departureTransportMeans.DepartureTransportMeansListSection
import pages.transport.ContainerIndicatorPage
import pages.transport.equipment.index._
import pages.transport.equipment.index.seals.SealIdentificationNumberPage
import pages.transport.equipment.{AddAnotherTransportEquipmentPage, AddTransportEquipmentYesNoPage, ItemPage}
import play.api.mvc.Call

@Singleton
class HouseConsignmentNavigator extends Navigator {

  override def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = ???

  override def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AddDepartureTransportMeansYesNoPage(houseConsignmentIndex) =>
      ua => addDepartureTransportMeansYesNoCheckRoute(ua, departureId, mode, houseConsignmentIndex)
    case IdentificationPage(houseConsignmentIndex, departureTransportMeansIndex) =>
      ua => IdentificationTypeCheckRoute(ua, departureId, mode, houseConsignmentIndex, departureTransportMeansIndex)
    case IdentificationNumberPage(houseConsignmentIndex, departureTransportMeansIndex) =>
      ua => IdentificationNumberCheckRoute(ua, departureId, mode, houseConsignmentIndex, departureTransportMeansIndex)
    case CountryPage(_, _) => _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
  }

  private def addDepartureTransportMeansYesNoCheckRoute(ua: UserAnswers, departureId: String, mode: Mode, houseConsignmentIndex: Index): Option[Call] = {
    val isIe015HCDepartureTransportMeansDefined =
      ua.departureData.Consignment.HouseConsignment.lift(houseConsignmentIndex.position).map(_.DepartureTransportMeans.isDefined)

    ua.get(AddDepartureTransportMeansYesNoPage(houseConsignmentIndex)) match {
      case Some(true) =>
        (ua.get(DepartureTransportMeansListSection(houseConsignmentIndex)), isIe015HCDepartureTransportMeansDefined) match {
          case (None, Some(false)) => IdentificationPage(houseConsignmentIndex, Index(0)).route(ua, departureId, CheckMode)
          case _                   => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
        }
      case _ => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))

    }

  }

  private def IdentificationTypeCheckRoute(ua: UserAnswers,
                                           departureId: String,
                                           mode: Mode,
                                           houseConsignmentIndex: Index,
                                           departureTransportMeansIndex: Index
  ): Option[Call] = {
    val ie015IdentificationNumber = ua.departureData.Consignment.HouseConsignment
      .lift(houseConsignmentIndex.position)
      .flatMap(
        _.DepartureTransportMeans.flatMap(
          seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.typeOfIdentification)
        )
      )

    (ua.get(IdentificationNumberPage(houseConsignmentIndex, departureTransportMeansIndex)), ie015IdentificationNumber) match {
      case (None, None) => IdentificationNumberPage(houseConsignmentIndex, departureTransportMeansIndex).route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }

  }

  private def IdentificationNumberCheckRoute(ua: UserAnswers,
                                             departureId: String,
                                             mode: Mode,
                                             houseConsignmentIndex: Index,
                                             departureTransportMeansIndex: Index
  ): Option[Call] = {
    val ie015Country = ua.departureData.Consignment.HouseConsignment
      .lift(houseConsignmentIndex.position)
      .flatMap(
        _.DepartureTransportMeans.flatMap(
          seq => seq.lift(departureTransportMeansIndex.position).flatMap(_.nationality)
        )
      )

    (ua.get(CountryPage(houseConsignmentIndex, departureTransportMeansIndex)), ie015Country) match {
      case (None, None) => CountryPage(houseConsignmentIndex, departureTransportMeansIndex).route(ua, departureId, CheckMode)
      case _            => Some(controllers.routes.CheckYourAnswersController.onPageLoad(departureId))
    }
  }

}
