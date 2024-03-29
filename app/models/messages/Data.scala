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

package models.messages

import models.departureP5.MessageType
import play.api.libs.json.{__, Reads}

case class Data(data: MessageData)

object Data {

  def reads(messageType: MessageType): Reads[Data] =
    (__ \ "body" \ s"n1:${messageType.dataPath}").read[MessageData].map(Data.apply)

}
