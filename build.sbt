import sbt.Keys._

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion = "2.5.11"
lazy val GatlingTest = config("gatling") extend Test

scalaVersion := "2.11.11"

resolvers += "dl-john-ky" at "http://dl.john-ky.io/maven/releases"

libraryDependencies ++= Seq(
  "com.netaporter" %% "scala-uri" % "0.4.14",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.5-play25",
  "com.github.pdorobisz" %% "math-expression-evaluator" % "1.0.0",
  "io.john-ky" %% "hashids-scala" % "1.1.2-2974446",
  filters,
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.3.0",
  ws,

  "com.digitaltangible" %% "play-guard" % "2.0.0",

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.pauldijou" %% "jwt-play-json" % "0.16.0",
  "com.sendgrid" % "sendgrid-java" % "4.1.2",
  "com.lihaoyi" %% "scalatags" % "0.6.7",


// Test
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5" % Test,
  "io.gatling" % "gatling-test-framework" % "2.2.5" % Test,
  "org.mockito" % "mockito-all" % "1.9.5" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0"
)

// The Play project itself
lazy val root = (project in file("."))
  .enablePlugins(Common, PlayScala, GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    name := """MATH_BOT""",
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation",
    watchSources ++= (baseDirectory.value / "public/ui" ** "*").get
  )

// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/index.html
lazy val docs = (project in file("docs")).enablePlugins(ParadoxPlugin).
  settings(
    paradoxProperties += ("download_url" -> "https://example.lightbend.com/v1/download/play-rest-api")
  )
