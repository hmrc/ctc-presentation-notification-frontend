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

import config.Constants.EntrySummaryDeclarationSecurityDetails
import models.Coordinates
import models.messages.AuthorisationType.{C521, Other}
import models.messages._
import play.api.libs.json.{JsValue, Json}

object TestMessageData {

  val limitDate = "2023-06-09"

  val transitOperation: TransitOperation =
    TransitOperation(None, Some(limitDate), EntrySummaryDeclarationSecurityDetails, reducedDatasetIndicator = "0")

  val seals: List[Seal] = List(Seal("1", "seal1"), Seal("2", "seal2"))

  val goodsReference: List[GoodsReference] = List(GoodsReference("1", 5))

  val transportEquipment: List[TransportEquipment] =
    List(TransportEquipment("26754", Some("CIN2"), 1234, Some(seals), Some(goodsReference)))

  val customsOffice: CustomsOffice =
    CustomsOffice("GB000068")

  val coordinates: Coordinates = Coordinates("44.968046", "-94.420307")

  val economicOperator: EconomicOperator = EconomicOperator("ECO247")

  val address: Address = Address("Address Line 1", Some("NE53KL"), "Newcastle", "GB")

  val postcodeAddress: PostcodeAddress = PostcodeAddress(Some("House number 1"), "NE52ZL", "GB")

  val contactName        = "Paul Sully"
  val contactPhoneNumber = "07508994566"
  val contactEmail       = "sullivan@epic.com"

  val contactPerson: ContactPerson = ContactPerson(contactName, contactPhoneNumber, Some(contactEmail))

  val representative: Representative = Representative("IdNumber", "2", Some(contactPerson))

  val locationOfGoods: LocationOfGoods = LocationOfGoods(
    "A",
    "Q",
    Some("AUTH1"),
    Some("ADD1"),
    Some("FG345UNLOCODE"),
    Some(customsOffice),
    Some(coordinates),
    Some(economicOperator),
    Some(address),
    Some(postcodeAddress),
    Some(contactPerson)
  )

  val activeBorderTransportMeansIdentification       = "10"
  val activeBorderTransportMeansIdentificationNumber = "BX857GGE"

  val activeBorderTransportMeans: List[ActiveBorderTransportMeans] = List(
    ActiveBorderTransportMeans(
      "11",
      Some("GB000028"),
      Some(activeBorderTransportMeansIdentification),
      Some(activeBorderTransportMeansIdentificationNumber),
      Some("FR"),
      Some("REF2")
    )
  )

  val departureTransportMeans: DepartureTransportMeans =
    DepartureTransportMeans(
      Some("10"),
      Some("BX857GGE"),
      Some("FR")
    )

  val placeOfLoading: PlaceOfLoading = PlaceOfLoading(Some("UNCODEX"), Some("GB"), Some("Sheffield"))

  val consignment: Consignment = Consignment(
    containerIndicator = Some("1"),
    inlandModeOfTransport = Some("3"),
    modeOfTransportAtTheBorder = Some("2"),
    TransportEquipment = Some(transportEquipment),
    LocationOfGoods = Some(locationOfGoods),
    DepartureTransportMeans = Some(departureTransportMeans),
    ActiveBorderTransportMeans = Some(activeBorderTransportMeans),
    PlaceOfLoading = Some(placeOfLoading),
    HouseConsignment = Seq(
      HouseConsignment(
        List(
          ConsignmentItem(
            "18914",
            1458,
            Commodity("descOfGoods")
          )
        )
      )
    )
  )

  val customsOfficeOfTransitDeclared: Option[Seq[CustomsOfficeOfTransitDeclared]] = Option(Seq(CustomsOfficeOfTransitDeclared("GB000013")))

  val authorisation: Seq[Authorisation] = Seq(
    Authorisation(C521, "AB123"),
    Authorisation(Other("SomethingElse"), "CD123")
  )

  val customsOfficeOfDeparture: String = "GB000011"

  val customsOfficeOfDestination: String = "GB000012"

  val holderOfTheTransitProcedure: HolderOfTheTransitProcedure = HolderOfTheTransitProcedure(
    identificationNumber = Some("identificationNumber"),
    TIRHolderIdentificationNumber = Some("TIRHolderIdentificationNumber"),
    ContactPerson = Some(ContactPerson("name", "phone", Some("email"))),
    Address = Some(Address("Address Line 1", Some("NE53KL"), "Newcastle", "GB"))
  )

  val messageData: MessageData =
    MessageData(
      customsOfficeOfDeparture,
      customsOfficeOfDestination,
      transitOperation,
      Some(authorisation),
      holderOfTheTransitProcedure,
      Some(representative),
      customsOfficeOfTransitDeclared,
      None,
      consignment
    )

  val jsonValue: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "CustomsOfficeOfTransitDeclared": [
       |        {
       |        "referenceNumber": "GB000013"
       |        }
       |    ],
       |    "TransitOperation": {
       |        "limitDate": "2023-06-09",
       |        "security": "1",
       |        "reducedDatasetIndicator": "0"
       |    },
       |    "Authorisation": [
       |        {
       |            "type": "C521",
       |            "referenceNumber": "AB123"
       |        },
       |        {
       |            "type": "SomethingElse",
       |            "referenceNumber": "CD123"
       |        }
       |    ],
       |    "HolderOfTheTransitProcedure": {
       |        "identificationNumber": "identificationNumber",
       |        "TIRHolderIdentificationNumber": "TIRHolderIdentificationNumber",
       |        "ContactPerson": {
       |            "name": "name",
       |            "phoneNumber": "phone",
       |            "eMailAddress": "email"
       |        },
       |        "Address": {
       |            "streetAndNumber": "Address Line 1",
       |            "postcode": "NE53KL",
       |            "city":
       |            "Newcastle",
       |            "country": "GB"
       |        }
       |    },
       |    "Representative": {
       |        "identificationNumber": "IdNumber",
       |        "status": "2",
       |        "ContactPerson": {
       |            "name": "Paul Sully",
       |            "phoneNumber": "07508994566",
       |            "eMailAddress": "sullivan@epic.com"
       |        }
       |    },
       |    "Consignment": {
       |        "containerIndicator": "1",
       |        "inlandModeOfTransport": "3",
       |        "modeOfTransportAtTheBorder": "2",
       |        "DepartureTransportMeans": [
       |         {
       |          "typeOfIdentification": "10",
       |          "identificationNumber" : "BX857GGE",
       |          "nationality" : "FR"
       |          }
       |        ],
       |        "TransportEquipment": [
       |            {
       |                "sequenceNumber": "26754",
       |                "containerIdentificationNumber": "CIN2",
       |                "numberOfSeals": 1234,
       |                "Seal": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "identifier": "seal1"
       |                    },
       |                    {
       |                        "sequenceNumber": "2",
       |                        "identifier": "seal2"
       |                    }
       |                ],
       |                "GoodsReference": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "declarationGoodsItemNumber": 5
       |                    }
       |                ]
       |            }
       |        ],
       |        "LocationOfGoods": {
       |            "typeOfLocation": "A",
       |            "qualifierOfIdentification": "Q",
       |            "authorisationNumber": "AUTH1",
       |            "additionalIdentifier": "ADD1",
       |            "UNLocode": "FG345UNLOCODE",
       |            "CustomsOffice": {
       |                "referenceNumber": "GB000068"
       |            },
       |            "GNSS": {
       |                "latitude": "44.968046",
       |                "longitude": "-94.420307"
       |            },
       |            "EconomicOperator": {
       |                "identificationNumber": "ECO247"
       |            },
       |            "Address": {
       |                "streetAndNumber": "Address Line 1",
       |                "postcode": "NE53KL",
       |                "city": "Newcastle",
       |                "country": "GB"
       |            },
       |            "PostcodeAddress": {
       |                "houseNumber": "House number 1",
       |                "postcode": "NE52ZL",
       |                "country": "GB"
       |            },
       |            "ContactPerson": {
       |                "name": "Paul Sully",
       |                "phoneNumber": "07508994566",
       |                "eMailAddress": "sullivan@epic.com"
       |            }
       |        },
       |        "ActiveBorderTransportMeans": [
       |            {
       |                "sequenceNumber": "11",
       |                "customsOfficeAtBorderReferenceNumber": "GB000028",
       |                "typeOfIdentification": "10",
       |                "identificationNumber": "BX857GGE",
       |                "nationality": "FR",
       |                "conveyanceReferenceNumber": "REF2"
       |            }
       |        ],
       |        "PlaceOfLoading": {
       |            "UNLocode": "UNCODEX",
       |            "country": "GB",
       |            "location": "Sheffield"
       |        },
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

  val jsonValueNormalNoLimitDate: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "CustomsOfficeOfTransitDeclared": [
       |        {
       |        "referenceNumber": "GB000013"
       |        }
       |    ],
       |    "TransitOperation": {
       |        "security": "1",
       |        "reducedDatasetIndicator": "indicatorString"
       |    },
       |    "Authorisation": [
       |        {
       |            "type": "C521",
       |            "referenceNumber": "AB123"
       |        },
       |        {
       |            "type": "SomethingElse",
       |            "referenceNumber": "CD123"
       |        }
       |    ],
       |    "HolderOfTheTransitProcedure": {
       |        "identificationNumber": "identificationNumber",
       |        "TIRHolderIdentificationNumber": "TIRHolderIdentificationNumber",
       |        "ContactPerson": {
       |            "name": "name",
       |            "phoneNumber": "phone",
       |            "eMailAddress": "email"
       |        },
       |        "Address": {
       |            "streetAndNumber": "Address Line 1",
       |            "postcode": "NE53KL",
       |            "city":
       |            "Newcastle",
       |            "country": "GB"
       |        }
       |    },
       |    "Representative": {
       |        "identificationNumber": "IdNumber",
       |        "status": "2",
       |        "ContactPerson": {
       |            "name": "Paul Sully",
       |            "phoneNumber": "07508994566",
       |            "eMailAddress": "sullivan@epic.com"
       |        }
       |    },
       |    "Consignment": {
       |        "containerIndicator": "1",
       |        "modeOfTransportAtTheBorder": "2",
       |        "inlandModeOfTransport": "2",
       |        "TransportEquipment": [
       |            {
       |                "sequenceNumber": "26754",
       |                "containerIdentificationNumber": "CIN2",
       |                "numberOfSeals": 1234,
       |                "Seal": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "identifier": "seal1"
       |                    },
       |                    {
       |                        "sequenceNumber": "2",
       |                        "identifier": "seal2"
       |                    }
       |                ],
       |                "GoodsReference": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "declarationGoodsItemNumber": 5
       |                    }
       |                ]
       |            }
       |        ],
       |        "LocationOfGoods": {
       |            "typeOfLocation": "A",
       |            "qualifierOfIdentification": "Q",
       |            "authorisationNumber": "AUTH1",
       |            "additionalIdentifier": "ADD1",
       |            "UNLocode": "FG345UNLOCODE",
       |            "CustomsOffice": {
       |                "referenceNumber": "GB000068"
       |            },
       |            "GNSS": {
       |                "latitude": "44.968046",
       |                "longitude": "-94.420307"
       |            },
       |            "EconomicOperator": {
       |                "identificationNumber": "ECO247"
       |            },
       |            "Address": {
       |                "streetAndNumber": "Address Line 1",
       |                "postcode": "NE53KL",
       |                "city": "Newcastle",
       |                "country": "GB"
       |            },
       |            "PostcodeAddress": {
       |                "houseNumber": "House number 1",
       |                "postcode": "NE52ZL",
       |                "country": "GB"
       |            },
       |            "ContactPerson": {
       |                "name": "Paul Sully",
       |                "phoneNumber": "07508994566",
       |                "eMailAddress": "sullivan@epic.com"
       |            }
       |        },
       |        "DepartureTransportMeans": [
       |           {
       |                "typeOfIdentification": "10",
       |                "identificationNumber": "BX857GGE",
       |                "nationality": "FR"
       |            }
       |        ],
       |        "ActiveBorderTransportMeans": [
       |            {
       |                "sequenceNumber": "11",
       |                "customsOfficeAtBorderReferenceNumber": "GB000028",
       |                "typeOfIdentification": "10",
       |                "identificationNumber": "BX857GGE",
       |                "nationality": "FR",
       |                "conveyanceReferenceNumber": "REF2"
       |            }
       |        ],
       |        "PlaceOfLoading": {
       |            "UNLocode": "UNCODEX",
       |            "country": "GB",
       |            "location": "Sheffield"
       |        },
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

  val jsonValueWithLrn: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "TransitOperation": {
       |        "limitDate": "2023-06-09",
       |        "security": "1",
       |        "LRN": "testLrn",
       |        "reducedDatasetIndicator": "0"
       |    },
       |    "Authorisation": [
       |        {
       |            "type": "C521",
       |            "referenceNumber": "AB123"
       |        },
       |        {
       |            "type": "SomethingElse",
       |            "referenceNumber": "CD123"
       |        }
       |    ],
       |    "HolderOfTheTransitProcedure": {
       |    },
       |    "Consignment": {
       |        "containerIndicator": "1",
       |        "inlandModeOfTransport": "3",
       |        "modeOfTransportAtTheBorder": "2",
       |         "DepartureTransportMeans": [
       |         {
       |          "typeOfIdentification": "10",
       |          "identificationNumber" : "BX857GGE",
       |          "nationality" : "FR"
       |          }
       |        ],
       |        "TransportEquipment": [
       |            {
       |                "sequenceNumber": "26754",
       |                "containerIdentificationNumber": "CIN2",
       |                "numberOfSeals": 1234,
       |                "Seal": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "identifier": "seal1"
       |                    },
       |                    {
       |                        "sequenceNumber": "2",
       |                        "identifier": "seal2"
       |                    }
       |                ],
       |                "GoodsReference": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "declarationGoodsItemNumber": 5
       |                    }
       |                ]
       |            }
       |        ],
       |        "LocationOfGoods": {
       |            "typeOfLocation": "A",
       |            "qualifierOfIdentification": "Q",
       |            "authorisationNumber": "AUTH1",
       |            "additionalIdentifier": "ADD1",
       |            "UNLocode": "FG345UNLOCODE",
       |            "CustomsOffice": {
       |                "referenceNumber": "GB000068"
       |            },
       |            "GNSS": {
       |                "latitude": "44.968046",
       |                "longitude": "-94.420307"
       |            },
       |            "EconomicOperator": {
       |                "identificationNumber": "ECO247"
       |            },
       |            "Address": {
       |                "streetAndNumber": "Address Line 1",
       |                "postcode": "NE53KL",
       |                "city": "Newcastle",
       |                "country": "GB"
       |            },
       |            "PostcodeAddress": {
       |                "houseNumber": "House number 1",
       |                "postcode": "NE52ZL",
       |                "country": "GB"
       |            },
       |            "ContactPerson": {
       |                "name": "Paul Sully",
       |                "phoneNumber": "07508994566",
       |                "eMailAddress": "sullivan@epic.com"
       |            }
       |        },
       |        "ActiveBorderTransportMeans": [
       |            {
       |                "sequenceNumber": "11",
       |                "customsOfficeAtBorderReferenceNumber": "GB000028",
       |                "typeOfIdentification": "10",
       |                "identificationNumber": "BX857GGE",
       |                "nationality": "FR",
       |                "conveyanceReferenceNumber": "REF2"
       |            }
       |        ],
       |        "PlaceOfLoading": {
       |            "UNLocode": "UNCODEX",
       |            "country": "GB",
       |            "location": "Sheffield"
       |        },
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

  val incompleteJsonValue: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "TransitOperation": {
       |        "limitDate": "2023-06-09",
       |        "security": "1",
       |        "reducedDatasetIndicator": "0"
       |    },
       |    "HolderOfTheTransitProcedure": {
       |    },
       |    "Consignment": {
       |        "containerIndicator": "1",
       |        "modeOfTransportAtTheBorder": "2",
       |        "TransportEquipment": [
       |            {
       |                "sequenceNumber": "26754",
       |                "containerIdentificationNumber": "CIN2",
       |                "numberOfSeals": 1234,
       |                "Seal": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "identifier": "seal1"
       |                    },
       |                    {
       |                        "sequenceNumber": "2",
       |                        "identifier": "seal2"
       |                    }
       |                ],
       |                "GoodsReference": [
       |                    {
       |                        "sequenceNumber": "1",
       |                        "declarationGoodsItemNumber": 5
       |                    }
       |                ]
       |            }
       |        ],
       |        "LocationOfGoods": {
       |            "typeOfLocation": "A",
       |            "qualifierOfIdentification": "Q",
       |            "authorisationNumber": "AUTH1",
       |            "additionalIdentifier": "ADD1",
       |            "UNLocode": "FG345UNLOCODE",
       |            "CustomsOffice": {
       |                "referenceNumber": "GB000068"
       |            },
       |            "GNSS": {
       |                "latitude": "44.968046",
       |                "longitude": "-94.420307"
       |            },
       |            "EconomicOperator": {
       |                "identificationNumber": "ECO247"
       |            },
       |            "Address": {
       |                "streetAndNumber": "Address Line 1",
       |                "postcode": "NE53KL",
       |                "city": "Newcastle",
       |                "country": "GB"
       |            },
       |            "PostcodeAddress": {
       |                "houseNumber": "House number 1",
       |                "postcode": "NE52ZL",
       |                "country": "GB"
       |            },
       |            "ContactPerson": {
       |                "name": "Paul Sully",
       |                "phoneNumber": "07508994566",
       |                "eMailAddress": "sullivan@epic.com"
       |            }
       |        },
       |        "PlaceOfLoading": {
       |            "UNLocode": "UNCODEX",
       |            "country": "GB",
       |            "location": "Sheffield"
       |        },
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

  val allOptionsNoneJsonValue: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "TransitOperation": {
       |        "security": "1",
       |        "reducedDatasetIndicator": "0"
       |    },
       |    "HolderOfTheTransitProcedure": {
       |    },
       |    "Consignment": {
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

  val allOptionsNoneReducedDatasetTrueJsonValue: JsValue = Json.parse(
    s"""
       |{
       |    "CustomsOfficeOfDeparture": {
       |        "referenceNumber": "GB000011"
       |    },
       |    "CustomsOfficeOfDestinationDeclared": {
       |        "referenceNumber": "GB000012"
       |    },
       |    "TransitOperation": {
       |        "security": "1",
       |        "reducedDatasetIndicator": "1"
       |    },
       |    "HolderOfTheTransitProcedure": {
       |    },
       |    "Consignment": {
       |        "HouseConsignment": [
       |            {
       |                "ConsignmentItem": [
       |                    {
       |                        "goodsItemNumber": "18914",
       |                        "declarationGoodsItemNumber": 1458,
       |                        "Commodity": {
       |                            "descriptionOfGoods": "descOfGoods"
       |                        }
       |                    }
       |                ]
       |            }
       |        ]
       |    }
       |}
       |""".stripMargin
  )

}
