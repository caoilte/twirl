import sbt._
import Keys._

object Build extends Build {
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  lazy val root =
    Project("twirl", file("."))
      .settings(general: _*)
      .settings(noPublishing: _*)
      .aggregate(twirlCompiler, sbtTwirl)

  lazy val twirlApi =
    Project("twirl-api", file("twirl-api"))
      .settings(general: _*)
      .settings(
        libraryDependencies ++= Seq(
          commonsLang,
          Test.specs
        ),
        crossScalaVersions := Seq("2.10.2")
      )

  lazy val twirlCompiler =
    Project("twirl-compiler", file("twirl-compiler"))
      .settings(general: _*)
      //.settings(apiPublishing: _*) // use this to publish to repo.spray.io as well
      .settings(
        libraryDependencies ++= Seq(
          scalaIO,
          Test.specs
        ),
        libraryDependencies <+= scalaVersion(scalaCompiler)
      )
      .dependsOn(twirlApi % "test")

  lazy val sbtTwirl =
    Project("sbt-twirl", file("sbt-twirl"))
      .settings(general: _*)
      //.settings(apiPublishing: _*) // use this to publish to repo.spray.io as well
      .settings(
        Keys.sbtPlugin := true
        // CrossBuilding.crossSbtVersions := Seq("0.12", "0.11.3", "0.11.2")
      )
      .dependsOn(twirlCompiler)

  val artifactoryHost = scala.util.Properties.envOrNone("ARTIFACTORY_HOST") getOrElse(throw new Exception("Please set ARTIFACTORY_HOST ENV"))
  val artifactoryPath = scala.util.Properties.envOrNone("ARTIFACTORY_PATH") getOrElse(throw new Exception("Please set ARTIFACTORY_PATH ENV"))
  val artifactoryUsername = scala.util.Properties.envOrNone("ARTIFACTORY_USERNAME") getOrElse(throw new Exception("Please set ARTIFACTORY_USERNAME ENV"))
  val artifactoryPassword = scala.util.Properties.envOrNone("ARTIFACTORY_PASSWORD") getOrElse(throw new Exception("Please set ARTIFACTORY_PASSWORD ENV"))

  lazy val general = seq(
    version               := IO.read(file("sbt-twirl/src/main/resources/twirl-version")).trim,
    homepage              := Some(new URL("https://github.com/spray/sbt-twirl")),
    organization          := "io.spray",
    organizationHomepage  := Some(new URL("http://spray.io")),
    startYear             := Some(2012),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaBinaryVersion   <<= scalaVersion(sV => if (CrossVersion.isStable(sV)) CrossVersion.binaryScalaVersion(sV) else sV),
    scalacOptions         := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    description           := "The Play framework Scala template engine, standalone and packaged as an SBT plugin",
    resolvers             += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
    scalaVersion          := "2.10.2",
    publishMavenStyle := true,
    publishTo := Some("Artifactory Realm" at "http://"+artifactoryHost+artifactoryPath),
    credentials := Seq(Credentials("Artifactory Realm", artifactoryHost, artifactoryUsername, artifactoryPassword))
  )

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )
}


