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

import controllers.locationOfGoods.routes
import models.{LocationOfGoodsIdentification, Mode, UserAnswers}
import pages.QuestionPage
import pages.locationOfGoods.contact.{NamePage, PhoneNumberPage}
import pages.sections.locationOfGoods.{LocationOfGoodsSection, QualifierOfIdentificationDetailsSection}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

trait BaseIdentificationPage extends QuestionPage[LocationOfGoodsIdentification] {

  override def path: JsPath = LocationOfGoodsSection.path \ toString

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(routes.IdentificationController.onPageLoad(departureId, mode))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[LocationOfGoodsIdentification], userAnswers: UserAnswers): Try[UserAnswers] = {

    val qualifierOfIdentificationDetailsSectionPaths = Set(
      JsPath \ "Consignment" \ "LocationOfGoods" \ "authorisationNumber",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "additionalIdentifier",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "UNLocode",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "CustomsOffice",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "GNSS",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "EconomicOperator",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "Address",
      JsPath \ "Consignment" \ "LocationOfGoods" \ "PostcodeAddress"
    )

    val IE15AddAdditionalIdentifierPath = JsPath \ "Consignment" \ "LocationOfGoods" \ "addAdditionalIdentifier"
    val IE15AdditionalIdentifierPath    = JsPath \ "Consignment" \ "LocationOfGoods" \ "additionalIdentifier"
    val IE15AddContactPath              = JsPath \ "Consignment" \ "LocationOfGoods" \ "addContact"
    val IE15ContactPersonPath           = JsPath \ "Consignment" \ "LocationOfGoods" \ "ContactPerson"
    val IE15TelephoneNumberPath         = JsPath \ "Consignment" \ "LocationOfGoods" \ "telephoneNumber"

    value match {
      case Some(_) =>
        userAnswers
          .remove(AdditionalIdentifierPage, IE15AddAdditionalIdentifierPath)
          .flatMap(_.remove(AddIdentifierYesNoPage, IE15AdditionalIdentifierPath))
          .flatMap(_.remove(AddContactYesNoPage, IE15AddContactPath))
          .flatMap(_.remove(NamePage, IE15ContactPersonPath))
          .flatMap(_.remove(PhoneNumberPage, IE15TelephoneNumberPath))
          .flatMap(_.remove(QualifierOfIdentificationDetailsSection, qualifierOfIdentificationDetailsSectionPaths))
          .flatMap(cleanup)

      case None => super.cleanup(value, userAnswers)
    }
  }
}

case object IdentificationPage extends BaseIdentificationPage {
  override def toString: String = "qualifierOfIdentification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredIdentificationPage)
}

case object InferredIdentificationPage extends BaseIdentificationPage {
  override def toString: String = "inferredQualifierOfIdentification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(IdentificationPage)
}
