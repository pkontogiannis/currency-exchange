name := "landoopExchange"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion = "2.5.19"
lazy val scalaTestVersion = "3.0.5"

lazy val circeVersion = "0.10.0"
lazy val circeExtra = "1.22.0"
lazy val catsVersion = "1.4.0"
lazy val scalaCheck = "1.14.0"
lazy val wireMockVersion = "2.21.0"


lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(

    version := "0.1",

    resolvers += Resolver.bintrayRepo("unisay", "maven"),

    Defaults.itSettings,

    libraryDependencies ++=
      Seq(
        // Akka HTTP
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,

        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "it,test",
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "it,test",
        "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "it,test",

        // Scala Test
        "org.scalatest" %% "scalatest" % scalaTestVersion % "it,test",
        "org.scalacheck" %% "scalacheck" % scalaCheck,

        // JSON Serialization Library
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "de.heikoseeberger" %% "akka-http-circe" % circeExtra,

        // Mock server
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % Test,

        // Mockito
        "org.mockito" % "mockito-all" % "1.10.19" % Test,

        // Logging dependencies
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
        "org.typelevel" %% "cats-core" % catsVersion
      )
  )

enablePlugins(UniversalPlugin)
