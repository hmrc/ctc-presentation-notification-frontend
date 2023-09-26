import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "ctc-presentation-notification-frontend"

lazy val root = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
        majorVersion := 0,
        scalaVersion := "2.13.8",
        name := appName,
        RoutesKeys.routesImport ++= Seq("models._"),
        TwirlKeys.templateImports ++= Seq(
              "play.twirl.api.HtmlFormat",
              "play.twirl.api.HtmlFormat._",
              "uk.gov.hmrc.govukfrontend.views.html.components._",
              "uk.gov.hmrc.hmrcfrontend.views.html.components._",
              "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
              "models.Mode",
              "views.html.helper.CSPNonce",
              "viewModels.{InputSize, LabelSize, LegendSize}",
              "templates._",
              "views.utils.ViewUtils._"
        ),
        PlayKeys.playDefaultPort := 10134,
        libraryDependencies ++= AppDependencies(),
        // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
        // suppress warnings in generated routes files
        scalacOptions ++= Seq(
              "-feature",
              "-language:implicitConversions",
              "-Wconf:src=routes/.*:s",
              "-Wconf:cat=unused-imports&src=html/.*:s",
        ),
        Concat.groups := Seq(
              "javascripts/application.js" -> group(Seq("javascripts/app.js"))
        ),
        uglifyCompressOptions := Seq("unused=false", "dead_code=false", "warnings=false"),
        Assets / pipelineStages := Seq(digest, concat, uglify),
        ThisBuild / useSuperShell := false,
        uglify / includeFilter := GlobFilter("application.js"),
        ThisBuild / scalafmtOnCompile := true
  )
