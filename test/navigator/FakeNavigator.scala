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

package navigator

import models.{Index, Mode, UserAnswers}
import navigation.*
import navigation.EquipmentsNavigator.EquipmentsNavigatorProvider
import pages.*
import play.api.mvc.Call

class FakeNavigator(desiredRoute: Call) extends Navigator {

  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute

  override protected def normalRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ =>
      _ => Some(desiredRoute)
  }

  override protected def checkRoutes(departureId: String, mode: Mode): PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ =>
      _ => Some(desiredRoute)
  }

}

class FakeLocationOfGoodsNavigator(desiredRoute: Call) extends LocationOfGoodsNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeContainerNavigator(desiredRoute: Call) extends ContainerNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute

}

class FakeLoadingNavigator(desiredRoute: Call) extends LoadingNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeDepartureTransportMeansNavigator(desiredRoute: Call) extends DepartureTransportMeansNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeBorderNavigator(desiredRoute: Call) extends BorderNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeEquipmentsNavigator(desiredRoute: Call, nextIndex: Index) extends EquipmentsNavigator(new FakeEquipmentNavigator(desiredRoute), nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeEquipmentsNavigatorProvider(desiredRoute: Call) extends EquipmentsNavigatorProvider(new FakeEquipmentNavigator(desiredRoute)) {
  override def apply(nextIndex: Index): EquipmentsNavigator = new FakeEquipmentsNavigator(desiredRoute, nextIndex)
}

class FakeEquipmentNavigator(desiredRoute: Call) extends EquipmentNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeRepresentativeNavigator(desiredRoute: Call) extends RepresentativeNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}
