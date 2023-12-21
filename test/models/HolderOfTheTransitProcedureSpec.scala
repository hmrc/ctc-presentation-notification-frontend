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

package models

import models.messages.{Address, ContactPerson, HolderOfTheTransitProcedure}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class HolderOfTheTransitProcedureSpec extends AnyFreeSpec with Matchers {

  val holderOfTransitProcedure = HolderOfTheTransitProcedure(
    identificationNumber = Some("identificationNumber"),
    TIRHolderIdentificationNumber = Some("TIRHolderIdentificationNumber"),
    ContactPerson = Some(ContactPerson("name", "phone", Some("email"))),
    Address = Some(Address("Address Line 1", Some("NE53KL"), "Newcastle", "GB"))
  )

  val jsonObject = Json.parse("""
      |{"identificationNumber":"identificationNumber","TIRHolderIdentificationNumber":"TIRHolderIdentificationNumber","ContactPerson":{"name":"name","phoneNumber":"phone","eMailAddress":"email"},"Address":{"streetAndNumber":"Address Line 1","postcode":"NE53KL","city":"Newcastle","country":"GB"}}
      |""".stripMargin)

  "must serialise correctly" in {

    Json.toJson(holderOfTransitProcedure) mustEqual jsonObject
  }

  "must deserialize correctly" in {
    jsonObject.as[HolderOfTheTransitProcedure] mustBe holderOfTransitProcedure
  }
}
