name := "sangria-pekko-streams"
organization := "org.sangria-graphql"

description := "Sangria pekko-streams integration"
homepage := Some(url("https://sangria-graphql.github.io/"))
organizationHomepage := Some(url("https://github.com/sangria-graphql"))

licenses := Seq(
  "Apache License, ASL Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)

ThisBuild / crossScalaVersions := Seq("2.12.20", "2.13.16")
ThisBuild / scalaVersion := crossScalaVersions.value.last

// sbt-github-actions needs configuration in `ThisBuild`
ThisBuild / crossScalaVersions := Seq("2.12.20", "2.13.16")
ThisBuild / scalaVersion := crossScalaVersions.value.tail.head
ThisBuild / githubWorkflowBuildPreamble ++= List(
  WorkflowStep.Sbt(List("scalafmtCheckAll"), name = Some("Check formatting"))
)

// Release
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/sangria-graphql/sangria-pekko-streams"),
    connection = "scm:git:git@github.com:sangria-graphql/sangria-pekko-streams.git"
  ))

lazy val root = (project in file("."))
  .settings(
    name := "sangria-pekko-streams",
    libraryDependencies ++= Seq(
      "org.sangria-graphql" %% "sangria-streaming-api" % "1.0.3",
      "org.apache.pekko" %% "pekko-stream" % "1.1.4",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
