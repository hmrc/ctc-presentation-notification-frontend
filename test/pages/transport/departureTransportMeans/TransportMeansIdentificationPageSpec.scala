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

package pages.transport.departureTransportMeans

import models.reference.transport.transportMeans.TransportMeansIdentification
import pages.behaviours.PageBehaviours
import pages.transport.transportMeans.TransportMeansIdentificationPage

class TransportMeansIdentificationPageSpec extends PageBehaviours {

  "IdentificationPage" - {

    beRetrievable[TransportMeansIdentification](TransportMeansIdentificationPage)

    beSettable[TransportMeansIdentification](TransportMeansIdentificationPage)

    beRemovable[TransportMeansIdentification](TransportMeansIdentificationPage)

    "cleanup" - {
      // TODO Add clean-up test
    }
  }
}