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

import config.Constants.AuthorisationTypeDeparture._
import config.Constants.DeclarationTypeSecurity._
import generated._
import models.reference.{Country, CountryCode, Item}
import play.api.libs.json._
import scalaxb.`package`.toScope
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.LocalDate
import javax.xml.datatype.XMLGregorianCalendar
import scala.annotation.nowarn
import scala.xml.NodeSeq

package object models {

  implicit class RichSensitiveString(sensitiveString: SensitiveString) {
    def decrypt: JsObject = Json.parse(sensitiveString.decryptedValue).as[JsObject]
  }

  implicit class RichJsArray(arr: JsArray) {

    def zipWithIndex: List[(JsValue, Index)] = arr.value.toList.zipWithIndex.map(
      x => (x._1, Index(x._2))
    )

  }

  implicit class RichJsObject(jsObject: JsObject) {

    def setObject(path: JsPath, value: JsValue): JsResult[JsObject] =
      jsObject.set(path, value).flatMap(_.validate[JsObject])

    def removeObject(path: JsPath): JsResult[JsObject] =
      jsObject.remove(path).flatMap(_.validate[JsObject])

    def removeObjectStandard(path: JsPath): JsResult[JsObject] = {
      val transformer = path.json.prune
      jsObject.transform(transformer)
    }
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def mapWithIndex[T](f: (JsValue, Index) => Option[T]): Seq[T] =
      arr
        .map {
          _.zipWithIndex.flatMap {
            case (value, i) => f(value, i)
          }
        }
        .getOrElse(Nil)

    def validate[T](implicit rds: Reads[T]): Option[T] =
      arr.flatMap(_.validate[T].asOpt)

    def length: Int = arr.getOrElse(JsArray()).value.length

  }

  implicit class RichJsValue(jsValue: JsValue) {

    def encrypt: SensitiveString = SensitiveString(Json.stringify(jsValue))

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case ((_: RecursiveSearch) :: _, _) =>
          JsError("recursive search not supported")

        case ((n: IdxPathNode) :: Nil, _) =>
          setIndexNode(n, jsValue, value)

        case ((n: KeyPathNode) :: Nil, _) =>
          setKeyNode(n, jsValue, value)

        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.set(JsPath(second :: rest), value).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }

    private def setIndexNode(node: IdxPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val index: Int = node.idx

      oldValue match {
        case oldValue: JsArray if index >= 0 && index <= oldValue.value.length =>
          if (index == oldValue.value.length) {
            JsSuccess(oldValue.append(newValue))
          } else {
            JsSuccess(JsArray(oldValue.value.updated(index, newValue)))
          }
        case oldValue: JsArray =>
          JsError(s"array index out of bounds: $index, $oldValue")
        case _ =>
          JsError(s"cannot set an index on $oldValue")
      }
    }

    private def removeIndexNode(node: IdxPathNode, valueToRemoveFrom: JsArray): JsResult[JsValue] = {
      val index: Int = node.idx

      valueToRemoveFrom match {
        case valueToRemoveFrom: JsArray if index >= 0 && index < valueToRemoveFrom.value.length =>
          val updatedJsArray = valueToRemoveFrom.value.slice(0, index) ++ valueToRemoveFrom.value.slice(index + 1, valueToRemoveFrom.value.size)
          JsSuccess(JsArray(updatedJsArray))
        case valueToRemoveFrom: JsArray => JsError(s"array index out of bounds: $index, $valueToRemoveFrom")
      }
    }

    private def setKeyNode(node: KeyPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val key = node.key

      oldValue match {
        case oldValue: JsObject =>
          JsSuccess(oldValue + (key -> newValue))
        case _ =>
          JsError(s"cannot set a key on $oldValue")
      }
    }

    @nowarn("msg=match may not be exhaustive")
    // scalastyle:off cyclomatic.complexity
    def remove(path: JsPath): JsResult[JsValue] =
      (path.path, jsValue) match {
        case (Nil, _)                                                                  => JsError("path cannot be empty")
        case ((n: KeyPathNode) :: Nil, value: JsObject) if value.keys.contains(n.key)  => JsSuccess(value - n.key)
        case ((n: KeyPathNode) :: Nil, value: JsObject) if !value.keys.contains(n.key) => JsSuccess(value)
        case ((n: IdxPathNode) :: Nil, value: JsArray)                                 => removeIndexNode(n, value)
        case ((_: KeyPathNode) :: Nil, _)                                              => JsError(s"cannot remove a key on $jsValue")
        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              (opt: Option[JsValue]) =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.remove(JsPath(second :: rest)).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }
    // scalastyle:on cyclomatic.complexity
  }

  implicit class RichString(string: String) {

    def removeSpaces(): String =
      string.foldLeft("") {
        (acc, c) =>
          acc + c.toString.trim
      }
  }

  implicit class RichSeqT[T](value: Seq[T]) {

    def toOption: Option[Seq[T]] = value match {
      case Nil => None
      case _   => Some(value)
    }
  }

  implicit class RichCC015CType(value: CC015CType) {

    def toXML: NodeSeq = scalaxb.toXML(value, CC015C.toString, toScope())

    def isSimplified: Boolean = value.Authorisation.exists(_.typeValue == ACR)

    def hasAuthC523: Boolean = value.Authorisation.exists(_.typeValue == SSE)

    private def isDataCompleteSimplified: Boolean = {
      val options: List[Option[?]] = List(
        value.TransitOperation.limitDate,
        value.Consignment.containerIndicator,
        value.Consignment.modeOfTransportAtTheBorder,
        value.Consignment.TransportEquipment.toOption,
        value.Consignment.LocationOfGoods,
        value.Consignment.ActiveBorderTransportMeans.toOption,
        value.Consignment.PlaceOfLoading
      )
      options.forall(_.isDefined)
    }

    private def isDataCompleteNormal: Boolean = {
      val options: List[Option[?]] = List(
        value.Consignment.containerIndicator,
        value.Consignment.modeOfTransportAtTheBorder,
        value.Consignment.TransportEquipment.toOption,
        value.Consignment.LocationOfGoods,
        value.Consignment.ActiveBorderTransportMeans.toOption,
        value.Consignment.PlaceOfLoading
      )
      options.forall(_.isDefined)
    }

    def isDataComplete: Boolean = if (isSimplified) isDataCompleteSimplified else isDataCompleteNormal

    def customsOffices: Seq[String] =
      Seq(
        Seq(value.CustomsOfficeOfDestinationDeclared.referenceNumber),
        value.CustomsOfficeOfTransitDeclared.map(_.referenceNumber),
        value.CustomsOfficeOfExitForTransitDeclared.map(_.referenceNumber)
      ).flatten

    def hasSecurity: Boolean =
      value.TransitOperation.security != NoSecurityDetails

    def countryOfDeparture: String =
      value.CustomsOfficeOfDeparture.referenceNumber.take(2)

    def items: Seq[Item] =
      value.Consignment.HouseConsignment
        .flatMap(_.ConsignmentItem.map {
          consignmentItem =>
            Item(consignmentItem.declarationGoodsItemNumber.toInt, consignmentItem.Commodity.descriptionOfGoods)
        })
        .sortBy(_.declarationGoodsItemNumber)
  }

  implicit class RichCC013CType(value: CC013CType) {

    def toCC015CType(lrn: LocalReferenceNumber): CC015CType = CC015CType(
      messageSequence1 = value.messageSequence1,
      TransitOperation = value.TransitOperation.toTransitOperationType06(lrn),
      Authorisation = value.Authorisation,
      CustomsOfficeOfDeparture = value.CustomsOfficeOfDeparture,
      CustomsOfficeOfDestinationDeclared = value.CustomsOfficeOfDestinationDeclared,
      CustomsOfficeOfTransitDeclared = value.CustomsOfficeOfTransitDeclared,
      CustomsOfficeOfExitForTransitDeclared = value.CustomsOfficeOfExitForTransitDeclared,
      HolderOfTheTransitProcedure = value.HolderOfTheTransitProcedure,
      Representative = value.Representative,
      Guarantee = value.Guarantee.flatMap(_.toGuaranteeType02),
      Consignment = value.Consignment,
      attributes = value.attributes
    )
  }

  implicit class RichGuaranteeType02(value: GuaranteeType01) {

    def toGuaranteeType02: Option[GuaranteeType02] = value.guaranteeType.map {
      guaranteeType =>
        GuaranteeType02(
          sequenceNumber = value.sequenceNumber,
          guaranteeType = guaranteeType,
          otherGuaranteeReference = value.otherGuaranteeReference,
          GuaranteeReference = value.GuaranteeReference
        )
    }
  }

  implicit class RichTransitOperationType04(value: TransitOperationType04) {

    def toTransitOperationType06(lrn: LocalReferenceNumber): TransitOperationType06 =
      TransitOperationType06(
        LRN = lrn.value,
        declarationType = value.declarationType,
        additionalDeclarationType = value.additionalDeclarationType,
        TIRCarnetNumber = value.TIRCarnetNumber,
        presentationOfTheGoodsDateAndTime = value.presentationOfTheGoodsDateAndTime,
        security = value.security,
        reducedDatasetIndicator = value.reducedDatasetIndicator,
        specificCircumstanceIndicator = value.specificCircumstanceIndicator,
        communicationLanguageAtDeparture = value.communicationLanguageAtDeparture,
        bindingItinerary = value.bindingItinerary,
        limitDate = value.limitDate
      )
  }

  implicit class RichGNSSType(value: GNSSType) {

    def toCoordinates: Coordinates = Coordinates(
      latitude = value.latitude,
      longitude = value.longitude
    )
  }

  implicit class RichAddressType14(value: AddressType14) {

    def toDynamicAddress: DynamicAddress = DynamicAddress(
      numberAndStreet = value.streetAndNumber,
      city = value.city,
      postalCode = value.postcode
    )
  }

  implicit class RichAddressType17(value: AddressType17) {

    def toDynamicAddress: DynamicAddress = DynamicAddress(
      numberAndStreet = value.streetAndNumber,
      city = value.city,
      postalCode = value.postcode
    )
  }

  implicit class RichPostcodeAddressType02(value: PostcodeAddressType02) {

    def toPostalCode(countryDescription: String): PostalCodeAddress = PostalCodeAddress(
      streetNumber = value.houseNumber.getOrElse(""),
      postalCode = value.postcode,
      country = Country(CountryCode(value.country), countryDescription)
    )
  }

  implicit def flagToBool(value: Flag): Boolean =
    value == Number1

  implicit def xmlGregorianCalendarToLocalDate(value: XMLGregorianCalendar): LocalDate =
    value.toGregorianCalendar.toZonedDateTime.toLocalDate
}
