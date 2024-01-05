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

package forms.behaviours

import forms.FormSpec
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {

  def fieldThatBindsValidData(form: Form[_], fieldName: String, validDataGenerator: Gen[String]): Unit =
    "must bind valid data" in {

      forAll(validDataGenerator -> "validDataItem") {
        dataItem: String =>
          val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
          result.value.value mustBe dataItem
      }
    }

  def fieldThatRemovesSpaces(form: Form[_], fieldName: String, validDataGenerator: Gen[String]): Unit =
    "must bind valid data and remove spaces" in {

      forAll(validDataGenerator -> "validDataItem") {
        dataItem: String =>
          val dataItemWithSpaces = dataItem.foldLeft("")({
            case (acc, c) =>
              acc + " " + c.toString + " "
          })
          dataItemWithSpaces must not be dataItem
          val result = form.bind(Map(fieldName -> dataItemWithSpaces))
          result.errors.foreach(println)
          result.value.value mustBe dataItem
      }
    }

  def mandatoryField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {

    "must not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "must not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def optionalField(form: Form[_], fieldName: String): Unit = {

    "must not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors must be(empty)
      result.value mustBe None
    }

    "must not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors must be(empty)
      result.value.value mustBe ""
    }
  }
}
