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

package pages.locationOfGoods

import models.{LocationOfGoodsIdentification, UserAnswers}
import pages.QuestionPage
import pages.sections.locationOfGoods.{LocationOfGoodsSection, QualifierOfIdentificationDetailsSection}
import play.api.libs.json.JsPath

import scala.util.Try

trait BaseLocationOfGoodsPage extends QuestionPage[LocationOfGoodsIdentification] {

  override def path: JsPath = LocationOfGoodsSection.path \ toString

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[LocationOfGoodsIdentification], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(QualifierOfIdentificationDetailsSection).flatMap(cleanup)
      case None    => super.cleanup(value, userAnswers)
    }
}

case object LocationOfGoodsPage extends BaseLocationOfGoodsPage {
  override def toString: String = "locationOfGoods"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(IdentificationPage)
}
