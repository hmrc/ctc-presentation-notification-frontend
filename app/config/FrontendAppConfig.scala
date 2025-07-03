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

package config

import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject() (config: Configuration, servicesConfig: MyServicesConfig) {

  val loginUrl: String         = config.get[String]("urls.login")
  val loginContinueUrl: String = config.get[String]("urls.loginContinue")

  val isPhase6Enabled: Boolean = config.get[Boolean]("feature-flags.phase-6-enabled")

  lazy val limitDateDaysBefore: Int = config.get[Int]("dates.limitDate.daysBefore")
  lazy val limitDateDaysAfter: Int  = config.get[Int]("dates.limitDate.daysAfter")

  lazy val maxActiveBorderTransports: Int = config.get[Int]("limits.maxActiveBorderTransports")
  lazy val maxSeals: Int                  = config.get[Int]("limits.maxSeals")
  lazy val maxTransportMeans: Int         = config.get[Int]("limits.maxTransportMeans")
  lazy val maxEquipmentNumbers: Int       = config.get[Int]("limits.maxEquipmentNumbers")
  lazy val maxItems: Int                  = config.get[Int]("limits.maxItems")

  lazy val contactHost: String = config.get[String]("contact-frontend.host")

  lazy val commonTransitConventionTradersUrl: String = config.get[Service]("microservice.services.common-transit-convention-traders").fullServiceUrl

  val eccEnrolmentSplashPage: String = config.get[String]("urls.eccEnrolmentSplashPage")
  lazy val nctsHelpdeskUrl: String   = config.get[String]("urls.nctsHelpdesk")

  lazy val referenceDataUrl: String = servicesConfig.fullServiceUrl("customs-reference-data")

  lazy val enrolmentKey: String           = config.get[String]("enrolment.key")
  lazy val enrolmentIdentifierKey: String = config.get[String]("enrolment.identifierKey")

  lazy val enrolmentProxyUrl: String = config.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl

  val hubUrl: String     = config.get[String]("urls.manageTransitMovementsFrontend")
  val serviceUrl: String = s"$hubUrl/what-do-you-want-to-do"

  lazy val cacheTtl: Int           = config.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val replaceIndexes: Boolean = config.get[Boolean]("feature-flags.replace-indexes")

  val asyncCacheApiExpiration: Int = config.get[Int]("async-cache-api.expiration")

  val signOutUrl: String = config.get[String]("urls.logoutContinue") + config.get[String]("urls.feedback")

  val encryptionKey: String                                       = config.get[String]("encryption.key")
  val encryptionEnabled: Boolean                                  = config.get[Boolean]("encryption.enabled")
  lazy val manageTransitMovementsDeclareNewDeclarationUrl: String = config.get[String]("urls.manageTransitMovementsDepartureDeclaration")
  lazy val feedbackUrl: String                                    = config.get[String]("urls.feedback")
  private lazy val manageTransitMovementsUrl: String              = config.get[String]("urls.manageTransitMovementsFrontend")
  lazy val manageTransitMovementsViewDeparturesUrl: String        = s"$manageTransitMovementsUrl/view-departure-declarations"
}
