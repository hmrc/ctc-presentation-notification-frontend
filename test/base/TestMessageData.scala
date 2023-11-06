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

package base

import config.Constants.NoSecurityDetails
import models.messages.AuthorisationType.{C521, Other}
import models.messages._
import play.api.libs.json.{JsValue, Json}

object TestMessageData {

  val transitOperation: TransitOperation =
    TransitOperation(Some("2023-06-09"), NoSecurityDetails)

  val seals: List[Seal] = List(Seal("1", "seal1"), Seal("2", "seal2"))

  val goodsReference: List[GoodsReference] = List(GoodsReference("1", 5))

  val transportEquipment: List[TransportEquipment] =
    List(TransportEquipment("26754", Some("CIN2"), 1234, Some(seals), Some(goodsReference)))

  val customsOffice: CustomsOffice =
    CustomsOffice("GB000068")

  val gnss: GNSS = GNSS("44.968046", "-94.420307")

  val economicOperator: EconomicOperator = EconomicOperator("ECO247")

  val address: Address = Address("Address Line 1", Some("NE53KL"), "Newcastle", "GB")

  val postcodeAddress: PostcodeAddress = PostcodeAddress(Some("House number 1"), "NE52ZL", "GB")

  val contactPerson: ContactPerson = ContactPerson("Paul Sully", "07508994566", Some("sullivan@epic.com"))

  val locationOfGoods: LocationOfGoods = LocationOfGoods(
    "A",
    "Q",
    Some("AUTH1"),
    Some("ADD1"),
    Some("FG345UNLOCODE"),
    Some(customsOffice),
    Some(gnss),
    Some(economicOperator),
    Some(address),
    Some(postcodeAddress),
    Some(contactPerson)
  )

  val activeBorderTransportMeans: List[ActiveBorderTransportMeans] = List(
    ActiveBorderTransportMeans(
      "11",
      Some("GB000028"),
      Some("10"),
      Some("BX857GGE"),
      Some("FR"),
      Some("REF2")
    )
  )

  val placeOfLoading: PlaceOfLoading = PlaceOfLoading(Some("UNCODEX"), Some("GB"), Some("Sheffield"))

  val consignment: Consignment = Consignment(
    Some("1"),
    Some("2"),
    Some(transportEquipment),
    Some(locationOfGoods),
    Some(activeBorderTransportMeans),
    Some(placeOfLoading)
  )

  val authorisation: Seq[Authorisation] = Seq(
    Authorisation(C521, "AB123"),
    Authorisation(Other("SomethingElse"), "CD123")
  )

  val customsOfficeOfDeparture: String = "GB000011"

  val customsOfficeOfDestination: String = "GB000012"

  val messageData: MessageData = MessageData(customsOfficeOfDeparture, customsOfficeOfDestination, transitOperation, Some(authorisation), consignment)

  val jsonValue: JsValue = Json.parse(
    s"""
       |{
       |   "CustomsOfficeOfDeparture":{
       |        "referenceNumber":"GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared":{
       |        "referenceNumber":"GB000012"
       |    },
       |   "TransitOperation" : {
       |       "limitDate" : "2023-06-09"
       |   },
       |   "Authorisation" : [
       |     {
       |       "type" : "C521",
       |       "referenceNumber": "AB123"
       |     },
       |     {
       |       "type": "SomethingElse",
       |       "referenceNumber": "CD123"
       |     }
       |   ],
       |   "Consignment" : {
       |       "containerIndicator" : "1",
       |       "modeOfTransportAtTheBorder": "2",
       |       "TransportEquipment": [
       |         {
       |           "sequenceNumber": "26754",
       |           "containerIdentificationNumber": "CIN2",
       |           "numberOfSeals": 1234,
       |           "Seal": [
       |             {
       |               "sequenceNumber": "1",
       |               "identifier": "seal1"
       |             },
       |             {
       |               "sequenceNumber": "2",
       |               "identifier": "seal2"
       |             }
       |           ],
       |           "GoodsReference": [
       |             {
       |               "sequenceNumber": "1",
       |               "declarationGoodsItemNumber": 5
       |             }
       |           ]
       |         }
       |       ],
       |       "LocationOfGoods": {
       |          "typeOfLocation": "A",
       |          "qualifierOfIdentification": "Q",
       |          "authorisationNumber": "AUTH1",
       |          "additionalIdentifier": "ADD1",
       |          "UNLocode": "FG345UNLOCODE",
       |          "CustomsOffice": {
       |            "referenceNumber": "GB000068"
       |          },
       |          "GNSS": {
       |            "latitude": "44.968046",
       |            "longitude": "-94.420307"
       |          },
       |          "EconomicOperator": {
       |            "identificationNumber": "ECO247"
       |          },
       |          "Address": {
       |            "streetAndNumber": "Address Line 1",
       |            "postcode": "NE53KL",
       |            "city": "Newcastle",
       |            "country": "GB"
       |          },
       |          "PostcodeAddress": {
       |            "houseNumber": "House number 1",
       |            "postcode": "NE52ZL",
       |            "country": "GB"
       |          },
       |          "ContactPerson": {
       |            "name": "Paul Sully",
       |            "phoneNumber": "07508994566",
       |            "eMailAddress": "sullivan@epic.com"
       |          }
       |       },
       |       "ActiveBorderTransportMeans": [
       |         {
       |           "sequenceNumber": "11",
       |           "customsOfficeAtBorderReferenceNumber": "GB000028",
       |           "typeOfIdentification": "10",
       |           "identificationNumber": "BX857GGE",
       |           "nationality": "FR",
       |           "conveyanceReferenceNumber": "REF2"
       |         }
       |       ],
       |       "PlaceOfLoading": {
       |         "UNLocode": "UNCODEX",
       |         "country": "GB",
       |         "location": "Sheffield"
       |       }
       |   }
       |}
       |""".stripMargin
  )

  val incompleteJsonValue: JsValue = Json.parse(
    s"""
       |{
       |
       |   "CustomsOfficeOfDeparture":{
       |        "referenceNumber":"GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared":{
       |        "referenceNumber":"GB000012"
       |    },
       |   "TransitOperation" : {
       |       "limitDate" : "2023-06-09"
       |   },
       |   "Consignment" : {
       |       "containerIndicator" : "1",
       |       "modeOfTransportAtTheBorder": "2",
       |       "TransportEquipment": [
       |         {
       |           "sequenceNumber": "26754",
       |           "containerIdentificationNumber": "CIN2",
       |           "numberOfSeals": 1234,
       |           "Seal": [
       |             {
       |               "sequenceNumber": "1",
       |               "identifier": "seal1"
       |             },
       |             {
       |               "sequenceNumber": "2",
       |               "identifier": "seal2"
       |             }
       |           ],
       |           "GoodsReference": [
       |             {
       |               "sequenceNumber": "1",
       |               "declarationGoodsItemNumber": 5
       |             }
       |           ]
       |         }
       |       ],
       |       "LocationOfGoods": {
       |          "typeOfLocation": "A",
       |          "qualifierOfIdentification": "Q",
       |          "authorisationNumber": "AUTH1",
       |          "additionalIdentifier": "ADD1",
       |          "UNLocode": "FG345UNLOCODE",
       |          "CustomsOffice": {
       |            "referenceNumber": "GB000068"
       |          },
       |          "GNSS": {
       |            "latitude": "44.968046",
       |            "longitude": "-94.420307"
       |          },
       |          "EconomicOperator": {
       |            "identificationNumber": "ECO247"
       |          },
       |          "Address": {
       |            "streetAndNumber": "Address Line 1",
       |            "postcode": "NE53KL",
       |            "city": "Newcastle",
       |            "country": "GB"
       |          },
       |          "PostcodeAddress": {
       |            "houseNumber": "House number 1",
       |            "postcode": "NE52ZL",
       |            "country": "GB"
       |          },
       |          "ContactPerson": {
       |            "name": "Paul Sully",
       |            "phoneNumber": "07508994566",
       |            "eMailAddress": "sullivan@epic.com"
       |          }
       |       },
       |       "PlaceOfLoading": {
       |         "UNLocode": "UNCODEX",
       |         "country": "GB",
       |         "location": "Sheffield"
       |       }
       |   }
       |}
       |""".stripMargin
  )
}
