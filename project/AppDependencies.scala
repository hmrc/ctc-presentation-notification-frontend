import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.22.0"
  private val hmrcMongoVersion = "1.3.0"
  private val catsVersion = "2.9.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "7.20.0-play-28",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "org.jsoup"               %  "jsoup"                      % "1.15.4"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test

  val overrides: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-kernel" % catsVersion
  )
}
