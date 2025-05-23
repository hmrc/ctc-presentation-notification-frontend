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
import navigation.BorderGroupNavigator.BorderGroupNavigatorProvider
import navigation.DepartureTransportMeansGroupNavigator.DepartureTransportMeansGroupNavigatorProvider
import navigation.EquipmentGroupNavigator.EquipmentGroupNavigatorProvider
import navigation.GoodsReferenceGroupNavigator.GoodsReferenceGroupNavigatorProvider
import navigation.SealGroupNavigator.SealGroupNavigatorProvider
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

class FakeDepartureTransportMeansGroupNavigator(desiredRoute: Call, nextIndex: Index) extends DepartureTransportMeansGroupNavigator(nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeDepartureTransportMeansGroupNavigatorProvider(desiredRoute: Call) extends DepartureTransportMeansGroupNavigatorProvider {
  override def apply(nextIndex: Index): DepartureTransportMeansGroupNavigator = new FakeDepartureTransportMeansGroupNavigator(desiredRoute, nextIndex)
}

class FakeDepartureTransportMeansNavigator(desiredRoute: Call) extends DepartureTransportMeansNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeBorderGroupNavigator(desiredRoute: Call, nextIndex: Index) extends BorderGroupNavigator(nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeBorderGroupNavigatorProvider(desiredRoute: Call) extends BorderGroupNavigatorProvider {
  override def apply(nextIndex: Index): BorderGroupNavigator = new FakeBorderGroupNavigator(desiredRoute, nextIndex)
}

class FakeBorderNavigator(desiredRoute: Call) extends BorderNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeEquipmentGroupNavigator(desiredRoute: Call, nextIndex: Index) extends EquipmentGroupNavigator(nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeEquipmentGroupNavigatorProvider(desiredRoute: Call) extends EquipmentGroupNavigatorProvider {
  override def apply(nextIndex: Index): EquipmentGroupNavigator = new FakeEquipmentGroupNavigator(desiredRoute, nextIndex)
}

class FakeEquipmentNavigator(desiredRoute: Call) extends EquipmentNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeSealGroupNavigator(desiredRoute: Call, nextIndex: Index) extends SealGroupNavigator(nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeSealGroupNavigatorProvider(desiredRoute: Call) extends SealGroupNavigatorProvider {
  override def apply(nextIndex: Index): SealGroupNavigator = new FakeSealGroupNavigator(desiredRoute, nextIndex)
}

class FakeSealNavigator(desiredRoute: Call) extends SealNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeGoodsReferenceGroupNavigator(desiredRoute: Call, nextIndex: Index) extends GoodsReferenceGroupNavigator(nextIndex) {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeGoodsReferenceGroupNavigatorProvider(desiredRoute: Call) extends GoodsReferenceGroupNavigatorProvider {
  override def apply(nextIndex: Index): GoodsReferenceGroupNavigator = new FakeGoodsReferenceGroupNavigator(desiredRoute, nextIndex)
}

class FakeGoodsReferenceNavigator(desiredRoute: Call) extends GoodsReferenceNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}

class FakeRepresentativeNavigator(desiredRoute: Call) extends RepresentativeNavigator {
  override def nextPage(page: Page, userAnswers: UserAnswers, departureId: String, mode: Mode): Call = desiredRoute
}
