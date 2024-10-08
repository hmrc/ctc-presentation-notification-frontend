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

package repositories

import config.FrontendAppConfig
import models.{SensitiveFormats, UserAnswers}
import org.mongodb.scala.model._
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala._

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext, sensitiveFormats: SensitiveFormats)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = "user-answers",
      domainFormat = UserAnswers.format,
      indexes = SessionRepository.indexes(appConfig),
      replaceIndexes = appConfig.replaceIndexes
    ) {

  def get(departureId: String): Future[Option[UserAnswers]] = {
    val filter = Filters.eq("_id", departureId)
    val update = Updates.set("lastUpdated", dateTimeService.currentInstant)

    collection
      .findOneAndUpdate(filter, update, FindOneAndUpdateOptions().upsert(false))
      .toFutureOption()
  }

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    val filter             = Filters.eq("_id", userAnswers.id)
    val updatedUserAnswers = userAnswers.copy(lastUpdated = dateTimeService.currentInstant)

    collection
      .replaceOne(filter, updatedUserAnswers, ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def remove(departureId: String): Future[Boolean] = {
    val filter = Filters.eq("_id", departureId)

    collection
      .deleteOne(filter)
      .toFuture()
      .map(_.wasAcknowledged())
  }
}

object SessionRepository {

  def indexes(appConfig: FrontendAppConfig): Seq[IndexModel] = {
    val userAnswersLastUpdatedIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().name("user-answers-last-updated-index").expireAfter(appConfig.cacheTtl.toLong, TimeUnit.SECONDS)
    )

    Seq(userAnswersLastUpdatedIndex)
  }

}
