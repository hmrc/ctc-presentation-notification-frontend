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

  lazy val contactHost: String = config.get[String]("contact-frontend.host")

  lazy val commonTransitConventionTradersUrl: String = config.get[Service]("microservice.services.common-transit-convention-traders").fullServiceUrl

  val eccEnrolmentSplashPage: String = config.get[String]("urls.eccEnrolmentSplashPage")
  lazy val nctsHelpdeskUrl: String   = config.get[String]("urls.nctsHelpdesk")

  lazy val referenceDataUrl: String = servicesConfig.fullServiceUrl("customs-reference-data")

  lazy val legacyEnrolmentKey: String           = config.get[String]("microservice.services.auth.legacy.enrolmentKey")
  lazy val legacyEnrolmentIdentifierKey: String = config.get[String]("microservice.services.auth.legacy.enrolmentIdentifierKey")

  lazy val newEnrolmentKey: String           = config.get[String]("microservice.services.auth.enrolmentKey")
  lazy val newEnrolmentIdentifierKey: String = config.get[String]("microservice.services.auth.enrolmentIdentifierKey")

  lazy val enrolmentProxyUrl: String = config.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl

  val hubUrl: String     = config.get[String]("urls.manageTransitMovementsFrontend")
  val serviceUrl: String = s"$hubUrl/what-do-you-want-to-do"

  lazy val cacheTtl: Int           = config.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val replaceIndexes: Boolean = config.get[Boolean]("feature-flags.replace-indexes")

  val signOutUrl: String = config.get[String]("urls.logoutContinue") + config.get[String]("urls.feedback")

  val encryptionKey: String      = config.get[String]("encryption.key")
  val encryptionEnabled: Boolean = config.get[Boolean]("encryption.enabled")
}
