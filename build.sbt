name := "sangria-pekko-streams"
organization := "org.sangria-graphql"

description := "Sangria pekko-streams integration"
homepage := Some(url("https://sangria-graphql.github.io/"))
licenses := Seq("Apache License, ASL Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / crossScalaVersions := Seq("2.12.20", "2.13.16")
ThisBuild / scalaVersion := crossScalaVersions.value.last

lazy val root = (project in file("."))
  .settings(
    name := "sangria-pekko-streams",
    libraryDependencies ++= Seq(
      "org.sangria-graphql" %% "sangria-streaming-api" % "1.0.3",
      "org.apache.pekko" %% "pekko-stream" % "1.1.4",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
