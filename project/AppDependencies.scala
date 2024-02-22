import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.3.0"
  private val hmrcMongoVersion = "1.6.0"
  private val catsVersion      = "2.9.0"
  private val monocleVersion   = "2.1.0"
  private val pekkoVersion = "1.0.1"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "com.chuusai"                %% "shapeless"                  % "2.3.10",
    "org.typelevel"              %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc"                %% "crypto-json-play-30"        % "7.6.0",
    "org.apache.commons"          % "commons-text"               % "1.10.0",
    "com.github.julien-truffaut" %% "monocle-core"               % "2.1.0",
    "com.github.julien-truffaut" %% "monocle-macro"              % monocleVersion,
    "org.apache.pekko"           %% "pekko-actor"                % pekkoVersion,
    "javax.xml.bind"              % "jaxb-api"                   % "2.3.1"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"         %% "scalatest"               % "3.2.17",
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.jsoup"              % "jsoup"                   % "1.15.4",
    "org.mockito"            % "mockito-core"            % "5.2.0",
    "org.scalatestplus"     %% "mockito-4-11"            % "3.2.17.0",
    "org.scalatestplus"     %% "scalacheck-1-17"         % "3.2.17.0",
    "org.scalacheck"        %% "scalacheck"              % "1.17.0",
    "io.github.wolfendale"  %% "scalacheck-gen-regexp"   % "1.1.0",
    "org.apache.pekko"      %% "pekko-testkit"          % pekkoVersion
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
