import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date

import Dependencies._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.Keys._
import sbt.Package.ManifestAttributes
import sbt.SbtExclusionRule

import scala.sys.process.Process
import scala.util.Try

val scalaV = "2.11.8"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

lazy val brandIntent = (project in file("brand-intent"))
  .settings(
    name := "brand-intent",
    libraryDependencies ++= apiDependencies.map(excludeLog4j)
  )
  .settings(withAssemblySettings: _*)
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
  .enablePlugins(JavaAppPackaging)

lazy val withAssemblySettings = projectSettings ++ assemblySettings

lazy val projectSettings = net.virtualvoid.sbt.graph.Plugin.graphSettings ++ Seq(
  version := "0.0.1-SNAPSHOT",
  organization := "in.ashwanthkumar",
  scalaVersion := scalaV,
  resolvers += Resolver.mavenLocal,
  excludeDependencies ++= Seq(
    SbtExclusionRule("cglib", "cglib-nodep"),
    SbtExclusionRule("commons-beanutils", "commons-beanutils"),
    SbtExclusionRule("commons-beanutils", "commons-beanutils-core")
  ),
  parallelExecution in ThisBuild := false,
  scalacOptions ++= Seq("-unchecked",
                        "-feature"
                        // , "-Ylog-classpath" // useful while debugging dependency classpath issues
  )
)

lazy val publishSettings = Seq(
  publishArtifact := true,
  packageOptions := Seq(
    ManifestAttributes(
      ("Built-By", InetAddress.getLocalHost.getHostName)
    )),
  crossScalaVersions := Seq(scalaV, "2.10.6"),
  publishMavenStyle := true,
  // disable publishing test jars
  publishArtifact in Test := false,
  // disable publishing the main docs jar
  publishArtifact in (Compile, packageDoc) := false,
  // disable publishing the main sources jar
  publishArtifact in (Compile, packageSrc) := true
)

lazy val assemblySettings = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if List("package-info.class", "plugin.properties", "mime.types").exists(ps.last.endsWith) =>
      MergeStrategy.first
    case "reference.conf" | "rootdoc.txt" =>
      MergeStrategy.concat
    case "LICENSE" | "LICENSE.txt" =>
      MergeStrategy.discard
    case PathList("META-INF", xs @ _*) =>
      xs map {
        _.toLowerCase
      } match {
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
          MergeStrategy.discard
        case ("log4j.properties" :: Nil) =>
          MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.deduplicate
  }
)
